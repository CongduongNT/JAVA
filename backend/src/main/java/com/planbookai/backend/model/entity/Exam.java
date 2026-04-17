package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
=======

>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

<<<<<<< HEAD
@Entity
@Table(name = "exams")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Exam {
=======
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

>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD
=======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    @Column(nullable = false)
    private String title;

    private String subject;

    @Column(name = "grade_level")
    private String gradeLevel;

<<<<<<< HEAD
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_points", precision = 5, scale = 2)
    private BigDecimal totalPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
=======
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
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

<<<<<<< HEAD
    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    @JdbcTypeCode(SqlTypes.JSON)
    private String settings;

    @Column(name = "created_at", updatable = false)
=======
    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    @Column(name = "created_at")
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
<<<<<<< HEAD
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

=======
    private LocalDateTime updatedAt;

    /** Danh sách câu hỏi trong đề (cascade xóa khi xóa đề). */
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();

<<<<<<< HEAD
    public enum ExamStatus { DRAFT, PUBLISHED, ARCHIVED }
=======
    public enum ExamStatus {
        DRAFT, PUBLISHED, CLOSED
    }
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
}
