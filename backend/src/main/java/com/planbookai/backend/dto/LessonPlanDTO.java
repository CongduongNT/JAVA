package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonPlanDTO {

    private String title;
    private String gradeLevel;
    private String subject;
    private String topic;
    private int durationMinutes;
    private List<String> objectives;
    private List<String> materials;
    private List<LessonPhase> lessonFlow;
    private Assessment assessment;
    private String homework;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonPhase {
        private String phase;
        private int timeMinutes;
        private String activities;
        private String teacherActions;
        private String studentActions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assessment {
        private List<String> methods;
        private String criteria;
    }
}