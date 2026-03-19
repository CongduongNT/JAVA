package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @Size(min = 1, max = 100, message = "Full name must be between 1 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9\\-\\+\\s]*$", message = "Phone number format is invalid")
    private String phone;

    @Size(max = 512, message = "Avatar URL must not exceed 512 characters")
    private String avatarUrl;
}
