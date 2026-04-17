package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * ExamQuestion – Bảng trung gian giữa {@link Exam} và {@link Question}.
 *
 * <p>Lưu thứ tự câu hỏi trong đề thi ({@code orderIndex}),
 * phiên bản đề ({@code versionNumber}) và điểm từng câu ({@code points}).
 */
@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "order_index")
    @Builder.Default
    private Integer orderIndex = 0;

    @Column(name = "version_number")
    @Builder.Default
    private Integer versionNumber = 1;

    /** Điểm của câu hỏi này trong đề thi. Mặc định 1.00. */
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal points = BigDecimal.ONE;
}
