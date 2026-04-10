package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonPlanDTO {

    private Long id;
    private String title;
    private String gradeLevel;
    private String subject;
    private String topic;
    private int durationMinutes;
    private String framework;
    private List<String> objectives;
    private List<String> materials;
    private List<LessonPhase> lessonFlow;
    private Assessment assessment;
    private String homework;
    private String notes;
    private Boolean aiGenerated;
    private Boolean isApproved;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LessonPhase {
        private String phase;
        private int timeMinutes;
        private String activities;
        private String teacherActions;
        private String studentActions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Assessment {
        private List<String> methods;
        private String criteria;
    }
}
