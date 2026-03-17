package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AuthResponse;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController – quản lý người dùng trong hệ thống.
 * Mỗi endpoint được bảo vệ bằng @PreAuthorize theo đúng role.
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    // =========================================================
    // ADMIN – quản lý toàn bộ user
    // =========================================================

    /**
     * [ADMIN] Lấy danh sách tất cả người dùng
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuthResponse.UserDTO>> getAllUsers() {
        List<AuthResponse.UserDTO> users = userRepository.findAll().stream()
                .map(u -> AuthResponse.UserDTO.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().getName().name() : null)
                        .isActive(u.getIsActive())
                        .build())
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * [ADMIN] Lấy thông tin một user theo ID
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse.UserDTO> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(AuthResponse.UserDTO.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole() != null ? u.getRole().getName().name() : null)
                        .isActive(u.getIsActive())
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * [ADMIN] Thay đổi role của một user
     */
    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String roleName = body.get("role");
        Role.RoleName roleEnum;
        try {
            roleEnum = Role.RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + roleName));
        }

        return userRepository.findById(id).map(user -> {
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            user.setRole(role);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Role updated successfully", "role", roleEnum.name()));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * [ADMIN] Kích hoạt / vô hiệu hóa user
     */
    @PutMapping("/admin/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        Boolean isActive = body.get("isActive");
        return userRepository.findById(id).map(user -> {
            user.setIsActive(isActive);
            userRepository.save(user);
            return ResponseEntity.ok(Map.<String, Object>of(
                    "message", "User status updated",
                    "isActive", isActive));
        }).orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // MANAGER – quản lý giáo viên / đơn hàng
    // =========================================================

    /**
     * [MANAGER] Lấy danh sách giáo viên (users có role TEACHER)
     */
    @GetMapping("/manager/teachers")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<AuthResponse.UserDTO>> getTeachers() {
        List<AuthResponse.UserDTO> teachers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null
                        && u.getRole().getName() == Role.RoleName.TEACHER)
                .map(u -> AuthResponse.UserDTO.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail())
                        .role(u.getRole().getName().name())
                        .isActive(u.getIsActive())
                        .build())
                .toList();
        return ResponseEntity.ok(teachers);
    }

    // =========================================================
    // STAFF – nội dung & prompt templates
    // =========================================================

    /**
     * [STAFF] Dashboard tổng quan dành cho nhân viên
     */
    @GetMapping("/staff/dashboard")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> staffDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Staff Dashboard",
                "description", "Manage AI prompt templates and question banks here."));
    }

    // =========================================================
    // TEACHER – tự xem profile của mình
    // =========================================================

    /**
     * [TEACHER] Lấy thông tin profile của bản thân
     */
    @GetMapping("/teacher/profile")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AuthResponse.UserDTO> getTeacherProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(AuthResponse.UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName().name() : null)
                .isActive(user.getIsActive())
                .build());
    }

    // =========================================================
    // ALL AUTH – tự xem / cập nhật profile
    // =========================================================


    /**
     * [ALL] Lấy profile của chính mình
     */
    @GetMapping("/users/me")
    public ResponseEntity<AuthResponse.UserDTO> getMyProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(AuthResponse.UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName().name() : null)
                .isActive(user.getIsActive())
                .build());
    }
}
