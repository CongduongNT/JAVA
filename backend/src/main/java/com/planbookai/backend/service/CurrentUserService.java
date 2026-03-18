package com.planbookai.backend.service;

import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // TODO: Integrate with real security context (SecurityContextHolder or @AuthenticationPrincipal)
    public Optional<User> getCurrentUserEntity() {
        // Fallback: return first user if exists
        return userRepository.findAll().stream().findFirst();
    }
}
