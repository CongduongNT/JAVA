package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private Boolean isActive;
    private Boolean emailVerified;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
