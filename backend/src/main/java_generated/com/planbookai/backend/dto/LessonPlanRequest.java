package com.planbookai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanRequest {

    @Positive(message = "frameworkId must be greater than 0")
    private Integer frameworkId;

    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must not exceed 255 characters")
    private String title;

    @Size(max = 100, message = "subject must not exceed 100 characters")
    private String subject;

    @Size(max = 50, message = "gradeLevel must not exceed 50 characters")
    private String gradeLevel;

    @Size(max = 255, message = "topic must not exceed 255 characters")
    private String topic;

    private String objectives;

    private String activities;

    private String assessment;

    private String materials;

    @Positive(message = "durationMinutes must be greater than 0")
    private Integer durationMinutes;
}
