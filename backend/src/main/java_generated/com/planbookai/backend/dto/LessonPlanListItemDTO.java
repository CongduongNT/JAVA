package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.LessonPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanListItemDTO {
    private Long id;
    private Integer frameworkId;
    private String title;
    private String subject;
    private String gradeLevel;
    private String topic;
    private Integer durationMinutes;
    private Boolean aiGenerated;
    private LessonPlan.LessonPlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
