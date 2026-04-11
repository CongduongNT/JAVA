package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.LessonPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanDTO {

    private Long id;
    private Long teacherId;
    private Integer frameworkId;
    private String title;
    private String subject;
    private String gradeLevel;
    private String topic;
    private String objectives;
    private String activities;
    private String assessment;
    private String materials;
    private Integer durationMinutes;

    private String framework;
    private List<String> lessonObjectives;
    private List<String> materialItems;
    private List<LessonPhase> lessonFlow;
    private AssessmentDetail assessmentDetail;
    private String homework;
    private String notes;

    private Boolean aiGenerated;
    private LessonPlan.LessonPlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonPhase {
        private String phase;
        private Integer timeMinutes;
        private String activities;
        private String teacherActions;
        private String studentActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssessmentDetail {
        private List<String> methods;
        private String criteria;
    }
}
