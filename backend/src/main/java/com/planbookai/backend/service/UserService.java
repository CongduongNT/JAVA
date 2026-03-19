package com.planbookai.backend.service;

import com.planbookai.backend.dto.ProfileResponse;
import com.planbookai.backend.dto.ProfileUpdateRequest;
import com.planbookai.backend.dto.UserRequest;
import com.planbookai.backend.dto.UserResponse;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, 
                       CurrentUserService currentUserService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currentUserService = currentUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(this::toResponse);
    }

    public UserResponse create(UserRequest req) {
        // Validate email unique and normalize
        String normalizedEmail = req.getEmail().toLowerCase().trim();
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email " + normalizedEmail + " is already in use");
        }
        
        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
        user.setAvatarUrl(null);
        user.setIsActive(true);
        user.setEmailVerified(false);
        
        // Validate role if provided
        if (req.getRoleId() != null) {
            Role role = roleRepository.findById(req.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role with ID " + req.getRoleId() + " not found"));
            user.setRole(role);
        }
        
        // Hash password before storing (trim password to avoid whitespace issues)
        user.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public Optional<UserResponse> update(Long id, UserRequest req) {
        return userRepository.findById(id).map(existing -> {
            // Normalize email for comparison and checking uniqueness
            String normalizedEmail = req.getEmail().toLowerCase().trim();
            String normalizedExistingEmail = existing.getEmail().toLowerCase().trim();
            
            // Validate email unique (if email changed)
            if (!normalizedExistingEmail.equals(normalizedEmail) && 
                userRepository.findByEmail(normalizedEmail).isPresent()) {
                throw new IllegalArgumentException("Email " + normalizedEmail + " is already in use");
            }
            
            existing.setFullName(req.getFullName().trim());
            existing.setEmail(normalizedEmail);
            existing.setPhone(req.getPhone() != null ? req.getPhone().trim() : null);
            
            // Validate role if provided
            if (req.getRoleId() != null) {
                Role role = roleRepository.findById(req.getRoleId())
                        .orElseThrow(() -> new IllegalArgumentException("Role with ID " + req.getRoleId() + " not found"));
                existing.setRole(role);
            }
            
            // Hash password before storing (only if provided, trim to avoid whitespace issues)
            if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
                existing.setPasswordHash(passwordEncoder.encode(req.getPassword().trim()));
            }
            
            User saved = userRepository.save(existing);
            return toResponse(saved);
        });
    }

    public boolean delete(Long id) {
        return userRepository.findById(id).map(u -> {
            userRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    public Optional<UserResponse> assignRole(Long userId, Integer roleId) {
        // Check if user exists
        return userRepository.findById(userId).map(u -> {
            // If roleId is not null, must validate role exists
            if (roleId != null) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role with ID " + roleId + " not found"));
                u.setRole(role);
            } else {
                // Allow role removal (set to null)
                u.setRole(null);
            }
            User saved = userRepository.save(u);
            return toResponse(saved);
        });
    }

    public Optional<ProfileResponse> findCurrentUserProfile() {
        return currentUserService.getCurrentUserEntity()
                .map(this::toProfileResponse);
    }

    public Optional<ProfileResponse> updateCurrentUserProfile(ProfileUpdateRequest req) {
        return currentUserService.getCurrentUserEntity().map(u -> {
            // Validation: check fields to update
            if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
                u.setFullName(req.getFullName().trim());
            }
            if (req.getPhone() != null && !req.getPhone().trim().isEmpty()) {
                u.setPhone(req.getPhone().trim());
            }
            if (req.getAvatarUrl() != null && !req.getAvatarUrl().trim().isEmpty()) {
                u.setAvatarUrl(req.getAvatarUrl().trim());
            }
            User saved = userRepository.save(u);
            return toProfileResponse(saved);
        });
    }

    private UserResponse toResponse(User u) {
        UserResponse res = new UserResponse();
        res.setId(u.getId());
        res.setFullName(u.getFullName());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setAvatarUrl(u.getAvatarUrl());
        res.setIsActive(u.getIsActive());
        res.setEmailVerified(u.getEmailVerified());
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        res.setRoleName(u.getRole() != null ? u.getRole().getName().name() : null);
        return res;
    }

    private ProfileResponse toProfileResponse(User u) {
        ProfileResponse res = new ProfileResponse();
        res.setId(u.getId());
        res.setFullName(u.getFullName());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setAvatarUrl(u.getAvatarUrl());
        res.setRoleName(u.getRole() != null ? u.getRole().getName().name() : null);
        res.setCreatedAt(u.getCreatedAt());
        res.setUpdatedAt(u.getUpdatedAt());
        return res;
    }
}
