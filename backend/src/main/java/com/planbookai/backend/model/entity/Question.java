package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Question – Câu hỏi trong ngân hàng câu hỏi.
 *
 * <p>Hỗ trợ 3 loại câu hỏi:
 * <ul>
 *   <li>MULTIPLE_CHOICE – trắc nghiệm (options là JSON array có is_correct)</li>
 *   <li>SHORT_ANSWER – trả lời ngắn</li>
 *   <li>FILL_IN_BLANK – điền vào chỗ trống</li>
 * </ul>
 *
 * <p>Trường {@code aiGenerated} đánh dấu câu hỏi được tạo bởi Gemini AI.
 */
@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private QuestionBank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private String topic;

    /**
     * JSON lưu danh sách đáp án cho câu trắc nghiệm.
     * Cấu trúc: [{label: "A", text: "...", isCorrect: true/false}, ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<Map<String, Object>> options;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    /**
     * Người phê duyệt câu hỏi (Manager).
     * Null nếu câu hỏi chưa được duyệt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum QuestionType {
        MULTIPLE_CHOICE, SHORT_ANSWER, FILL_IN_BLANK
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public long getTotalElements() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTotalElements'");
    }
}
