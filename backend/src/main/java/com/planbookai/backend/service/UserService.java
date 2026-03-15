package com.planbookai.backend.service;

import com.planbookai.backend.dto.UserRequest;
import com.planbookai.backend.dto.UserResponse;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        // password handling is deferred (not hashing here per request)
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
}
