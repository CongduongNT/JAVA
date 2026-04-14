package com.planbookai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumFrameworkRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 100, message = "Subject must not exceed 100 characters")
    private String subject;

    @Size(max = 50, message = "Grade level must not exceed 50 characters")
    private String gradeLevel;

    private String description;

    private String structure;

    private Boolean isPublished;
}
