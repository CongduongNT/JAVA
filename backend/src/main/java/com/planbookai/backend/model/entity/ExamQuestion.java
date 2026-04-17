package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ExamQuestion {
=======

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

>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

<<<<<<< HEAD
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(precision = 5, scale = 2)
    private BigDecimal points;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
=======
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
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
