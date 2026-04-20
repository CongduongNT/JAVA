package com.planbookai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemConfigRequest {
    @NotBlank(message = "configValue must not be blank")
    private String configValue;
}
