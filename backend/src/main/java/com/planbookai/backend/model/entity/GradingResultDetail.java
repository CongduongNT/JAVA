package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * GradingResultDetail – Chi tiết từng câu hỏi trong 1 kết quả chấm.
 *
 * <p>1 row = 1 câu hỏi mà học sinh đã trả lời (hoặc bỏ trống).
 * Liên kết về parent qua grading_result_id.
 */
@Entity
@Table(name = "grading_result_details",
       uniqueConstraints = @UniqueConstraint(columnNames = {"grading_result_id", "exam_question_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingResultDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grading_result_id", nullable = false)
    private GradingResult gradingResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_question_id", nullable = false)
    private ExamQuestion examQuestion;

    /** Question ID – denormalized để FE chi tiết không cần join nhiều bảng. */
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** Đáp án OCR đã đọc được – có thể null nếu học sinh bỏ trống. */
    @Column(name = "ocr_answer_text", columnDefinition = "TEXT")
    private String ocrAnswerText;

    /** Copy đáp án đúng từ Question – để hiển thị không cần join. */
    @Column(name = "correct_answer_text", columnDefinition = "TEXT")
    private String correctAnswerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private ResultStatus resultStatus;

    /** Điểm nhận được cho câu này. */
    @Column(name = "points_earned", nullable = false)
    @Builder.Default
    private BigDecimal pointsEarned = BigDecimal.ZERO;

    /** Điểm tối đa cho câu này. */
    @Column(name = "points_possible", nullable = false)
    @Builder.Default
    private BigDecimal pointsPossible = BigDecimal.ZERO;

    /** Độ tin cậy OCR: 0.000 → 1.000. NULL nếu OCR không trả về. */
    @Column(name = "ocr_confidence")
    private BigDecimal ocrConfidence;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ResultStatus {
        CORRECT, WRONG, BLANK, PARTIAL
    }
}
