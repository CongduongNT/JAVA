package com.planbookai.backend.dto;

import lombok.Data;

@Data
public class PromptTemplateRequest {
    private String title;
    private String purpose;
    private String promptText;
    private String variables;
}