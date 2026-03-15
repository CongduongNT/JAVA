package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AuthResponse;
import com.planbookai.backend.dto.LoginRequest;
import com.planbookai.backend.dto.RefreshTokenRequest;
import com.planbookai.backend.dto.RegisterRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Đối với JWT stateless, logout chủ yếu là công việc của client (xóa token).
        // Endpoint này tồn tại để hoàn thiện API contract.
        // Các hệ thống phức tạp hơn có thể thêm token vào một "danh sách đen" (blocklist).
        SecurityContextHolder.clearContext(); // Xóa context bảo mật phía server
        return ResponseEntity.ok("Logout successful. Please clear your tokens on the client-side.");
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal sẽ tự động inject đối tượng User từ SecurityContext
        if (user == null) {
            // Trả về 401 Unauthorized nếu không có user nào được xác thực
            return ResponseEntity.status(401).build();
        }
        // Xây dựng DTO để chỉ trả về thông tin cần thiết
        AuthResponse.UserDTO userDTO = AuthResponse.UserDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName().name() : null).build();
        return ResponseEntity.ok(userDTO);
    }
}