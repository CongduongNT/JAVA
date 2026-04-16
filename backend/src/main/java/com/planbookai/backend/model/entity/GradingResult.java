package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * GradingResult – Tổng hợp kết quả chấm của 1 học sinh cho 1 bài thi.
 *
 * <p>BR-01: 1 student × 1 exam = 1 row. Dùng upsert khi re-grade.
 *
 * <p>Liên kết:
 * <ul>
 *   <li>exam_id → exams</li>
 *   <li>teacher_id → users (chủ đề thi)</li>
 *   <li>1 → N grading_result_details</li>
 * </ul>
 */
@Entity
@Table(name = "grading_results",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID học sinh (refer bảng users.role=STUDENT). */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** Tên học sinh – denormalized để tránh join mỗi lần FE load list. */
    @Column(name = "student_name", nullable = false)
    private String studentName;

    /** Mã học sinh – nullable, không phải hệ thống nào cũng có. */
    @Column(name = "student_code")
    private String studentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    /** Chủ đề thi – lấy từ exam.teacher_id. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /** Tổng điểm thực nhận được. */
    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private BigDecimal totalScore = BigDecimal.ZERO;

    /** Tổng điểm tối đa của bài thi. */
    @Column(name = "total_possible", nullable = false)
    @Builder.Default
    private BigDecimal totalPossible = BigDecimal.ZERO;

    /** Phần trăm điểm: (total_score / total_possible) × 100, 2 decimal. */
    @Column(nullable = false)
    @Builder.Default
    private BigDecimal percentage = BigDecimal.ZERO;

    @Column(name = "correct_count", nullable = false)
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "wrong_count", nullable = false)
    @Builder.Default
    private Integer wrongCount = 0;

    @Column(name = "blank_count", nullable = false)
    @Builder.Default
    private Integer blankCount = 0;

    /** Nhận xét của giáo viên – do teacher nhập tay, có thể copy từ AI. */
    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    /** Gợi ý từ AI – lưu tạm sau khi AI sinh, FE hiển thị để teacher chọn. */
    @Column(name = "ai_feedback_suggestion", columnDefinition = "TEXT")
    private String aiFeedbackSuggestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_source")
    private FeedbackSource feedbackSource;

    @Column(name = "graded_at", nullable = false)
    @Builder.Default
    private LocalDateTime gradedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "graded_by", nullable = false)
    @Builder.Default
    private GradedBy gradedBy = GradedBy.SYSTEM;

    @Column(name = "last_updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "gradingResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GradingResultDetail> details = new ArrayList<>();

    public enum FeedbackSource {
        MANUAL, AI_EDITED
    }

    public enum GradedBy {
        SYSTEM, TEACHER
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
