package com.planbookai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * QuestionBankRequest – Body để tạo / cập nhật ngân hàng câu hỏi.
 */
@Data
public class QuestionBankRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String subject;
    private String gradeLevel;
    private String description;
    private Boolean isPublished;
}
