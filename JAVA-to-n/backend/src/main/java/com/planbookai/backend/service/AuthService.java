package com.planbookai.backend.service;

import com.planbookai.backend.Security.JwtTokenProvider;
import com.planbookai.backend.dto.AuthResponse;
import com.planbookai.backend.dto.LoginRequest;
import com.planbookai.backend.dto.RefreshTokenRequest;
import com.planbookai.backend.dto.RegisterRequest;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // Sửa lỗi: Chỉ giữ lại một Constructor duy nhất để Spring Injection
    public AuthService(UserRepository userRepository, 
                       RoleRepository roleRepository, 
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi lưu vào DB 
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Lấy Role từ database dựa trên Enum RoleName
        Role defaultRole = roleRepository.findByName(Role.RoleName.TEACHER)
                .orElseThrow(() -> new RuntimeException("Role TEACHER not found. Please seed the database."));

        // Sử dụng Builder pattern để tạo thực thể User sạch sẽ
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Mã hóa mật khẩu
                .role(defaultRole)
                .build();

        userRepository.save(user); // JPA thực hiện persist() xuống database 

        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Tìm user bằng email qua Repository (JPA Query) 
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    // Kiểm tra mật khẩu đã mã hóa
                    if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        return generateAuthResponse(user);
                    }
                    throw new RuntimeException("Wrong password");
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        if (jwtTokenProvider.validateToken(requestRefreshToken)) {
            String email = jwtTokenProvider.getEmailFromToken(requestRefreshToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return generateAuthResponse(user);
        }
        throw new RuntimeException("Invalid Refresh Token");
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName().name() : null)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }
}