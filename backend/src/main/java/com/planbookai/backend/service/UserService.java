package com.planbookai.backend.service;

import com.planbookai.backend.dto.ProfileResponse;
import com.planbookai.backend.dto.ProfileUpdateRequest;
import com.planbookai.backend.dto.UserRequest;
import com.planbookai.backend.dto.UserResponse;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currentUserService = currentUserService;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(this::toResponse);
    }

    public UserResponse create(UserRequest req) {
        User user = new User();
        user.setId(null);
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setAvatarUrl(null);
        user.setIsActive(true);
        user.setEmailVerified(false);
        if (req.getRoleId() != null) {
            Role role = roleRepository.findById(req.getRoleId()).orElse(null);
            user.setRole(role);
        }
        user.setPasswordHash(req.getPassword());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public Optional<UserResponse> update(Long id, UserRequest req) {
        return userRepository.findById(id).map(existing -> {
            existing.setFullName(req.getFullName());
            existing.setEmail(req.getEmail());
            existing.setPhone(req.getPhone());
            if (req.getRoleId() != null) {
                Role role = roleRepository.findById(req.getRoleId()).orElse(null);
                existing.setRole(role);
            }
            if (req.getPassword() != null && !req.getPassword().isEmpty()) {
                existing.setPasswordHash(req.getPassword());
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
        // Kiểm tra user tồn tại
        return userRepository.findById(userId).map(u -> {
            // Nếu roleId không null, phải kiểm tra role tồn tại
            if (roleId != null) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role with ID " + roleId + " not found"));
                u.setRole(role);
            } else {
                // Cho phép xóa role (set null)
                u.setRole(null);
            }
            User saved = userRepository.save(u);
            return toResponse(saved);
        });
    }

    public Optional<ProfileResponse> findCurrentUserProfile() {
        return currentUserService.getCurrentUserEntity().map(this::toProfileResponse);
    }

    public Optional<ProfileResponse> updateCurrentUserProfile(ProfileUpdateRequest req) {
        return currentUserService.getCurrentUserEntity().map(u -> {
            if (req.getFullName() != null) u.setFullName(req.getFullName());
            if (req.getPhone() != null) u.setPhone(req.getPhone());
            if (req.getAvatarUrl() != null) u.setAvatarUrl(req.getAvatarUrl());
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
