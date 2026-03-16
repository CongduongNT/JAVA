package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;

    @Data
    @Builder
    public static class UserDTO {
        private String fullName;
        private String email;
        private String role;
    }
}