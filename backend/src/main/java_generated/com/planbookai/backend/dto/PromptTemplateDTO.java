package com.planbookai.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PromptTemplateDTO {
    private Long id;
    private String title;
    private String purpose;
    private String promptText;
    private String variables;
    private String status;
    private String createdByName;
    private LocalDateTime createdAt;
}