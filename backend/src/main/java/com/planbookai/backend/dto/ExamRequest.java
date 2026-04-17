package com.planbookai.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamRequest {
    private String title;
    private String subject;
    private String gradeLevel;
    private String description;
    private Integer durationMinutes;
    private BigDecimal totalPoints;
    private String status;
    private String settings;
    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem {
        private Long questionId;
        private Integer orderIndex;
        private BigDecimal points;
    }
}
