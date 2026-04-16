package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GradingResultSummaryDTO – Danh sách kết quả chấm (1 row = 1 học sinh).
 * Dùng cho GET /api/v1/grading-results?exam_id={id}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingResultSummaryDTO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private Long examId;
    private BigDecimal totalScore;
    private BigDecimal totalPossible;
    private BigDecimal percentage;
    private int correctCount;
    private int wrongCount;
    private int blankCount;
    private boolean hasFeedback;
    private LocalDateTime gradedAt;
}
