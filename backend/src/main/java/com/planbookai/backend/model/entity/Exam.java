package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exam – Đề thi được tạo thủ công hoặc bởi AI.
 *
 * <p>Liên kết với bảng {@code exams} và có danh sách câu hỏi thông qua
 * bảng trung gian {@link ExamQuestion}.
 *
 * <p>Trường {@code aiGenerated} đánh dấu đề thi được tạo bởi AI.
 */
@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private String title;

    private String subject;

    @Column(name = "grade_level")
    private String gradeLevel;

    private String topic;

    @Column(name = "total_questions")
    @Builder.Default
    private Integer totalQuestions = 0;

    @Column(name = "duration_mins")
    private Integer durationMins;

    @Builder.Default
    private Boolean randomized = false;

    @Column(name = "version_count")
    @Builder.Default
    private Integer versionCount = 1;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Danh sách câu hỏi trong đề (cascade xóa khi xóa đề). */
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    public enum ExamStatus {
        DRAFT, PUBLISHED, CLOSED
    }
}
