package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * GradingFeedbackRequest – Request gửi cho Gemini AI để sinh gợi ý feedback.
 *
 * <p>Chỉ chứa dữ liệu CÓ THẬT từ bài làm.
 * KHÔNG suy đoán hay bịa đặt nguyên nhân.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingFeedbackRequest {

    private String examTitle;
    private String subject;
    private String gradeLevel;
    private String studentName;
    private BigDecimal totalScore;
    private BigDecimal totalPossible;
    private BigDecimal percentage;
    private int correctCount;
    private int wrongCount;
    private int blankCount;

    /** Câu sai: có đáp án học sinh và đáp án đúng để so sánh. */
    private List<WrongQuestionInfo> wrongQuestions;

    /** Câu bỏ trống: không có đáp án. */
    private List<BlankQuestionInfo> blankQuestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongQuestionInfo {
        private int order;
        private String questionContent;
        private String studentAnswer;
        private String correctAnswer;
        private BigDecimal pointsEarned;
        private BigDecimal pointsPossible;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlankQuestionInfo {
        private int order;
        private String questionContent;
    }
}
