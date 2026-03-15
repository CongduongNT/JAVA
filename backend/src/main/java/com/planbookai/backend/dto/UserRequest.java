package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    // plain password field for input (service will decide handling)
    @Size(min = 6)
    private String password;

    private String phone;

    private Integer roleId;
}
