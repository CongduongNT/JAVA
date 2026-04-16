package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GradingResultDetailDTO – Chi tiết đầy đủ 1 kết quả chấm (gồm từng câu).
 * Dùng cho GET /api/v1/grading-results/{id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResultDetailDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Long examId;
    private String examTitle;
    private BigDecimal totalScore;
    private BigDecimal totalPossible;
    private BigDecimal percentage;
    private int correctCount;
    private int wrongCount;
    private int blankCount;
    private String teacherFeedback;
    private String aiFeedbackSuggestion;
    private String feedbackSource;
    private LocalDateTime gradedAt;
    private String gradedBy;
    private List<GradingQuestionDetailDTO> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradingQuestionDetailDTO {
        private Long id;
        private Long examQuestionId;
        private Long questionId;
        private int orderIndex;
        private String questionContent;
        private String questionType;
        private String ocrAnswerText;
        private String correctAnswerText;
        private String resultStatus;
        private BigDecimal pointsEarned;
        private BigDecimal pointsPossible;
        private BigDecimal ocrConfidence;
    }
}
