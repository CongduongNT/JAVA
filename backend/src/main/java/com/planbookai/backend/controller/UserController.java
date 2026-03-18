package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ProfileResponse;
import com.planbookai.backend.dto.ProfileUpdateRequest;
import com.planbookai.backend.dto.RoleAssignRequest;
import com.planbookai.backend.dto.UserRequest;
import com.planbookai.backend.dto.UserResponse;
import com.planbookai.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        Optional<UserResponse> userOpt = userService.findById(id);
        return userOpt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest user) {
        UserResponse created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest user) {
        Optional<UserResponse> updated = userService.update(id, user);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Assign role to user
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> assignRole(@PathVariable Long id, @RequestBody RoleAssignRequest req) {
        Optional<UserResponse> updated = userService.assignRole(id, req.getRoleId());
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Get current authenticated user's profile
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMe() {
        Optional<ProfileResponse> me = userService.findCurrentUserProfile();
        return me.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Update current authenticated user's profile (partial updates allowed)
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMe(@Valid @RequestBody ProfileUpdateRequest req) {
        Optional<ProfileResponse> updated = userService.updateCurrentUserProfile(req);
        return updated.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = userService.delete(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
