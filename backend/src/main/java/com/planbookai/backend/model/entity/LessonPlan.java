package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * LessonPlan – Giáo án do AI sinh ra hoặc teacher tạo.
 */
@Entity
@Table(name = "lesson_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "grade_level", nullable = false)
    private String gradeLevel;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String topic;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Khung phương pháp giảng dạy: E5, E3, E4, BACKWARD_DESIGN, TGAP.
     */
    @Column(name = "framework")
    private String framework;

    /**
     * Mục tiêu bài học – lưu dạng JSON array.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> objectives;

    /**
     * Vật dụng/giáo án – lưu dạng JSON array.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> materials;

    /**
     * Cấu trúc các pha hoạt động – lưu dạng JSON array.
     * Mỗi phần tử: {phase, timeMinutes, activities, teacherActions, studentActions}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lesson_flow", columnDefinition = "JSON")
    private List<Map<String, Object>> lessonFlow;

    /**
     * Đánh giá – lưu dạng JSON object: {methods: [], criteria: ""}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> assessment;

    @Column(name = "homework", columnDefinition = "TEXT")
    private String homework;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Giáo án này có được AI sinh ra không.
     */
    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    /**
     * Trạng thái phê duyệt (chỉ dùng cho giáo án do AI sinh).
     */
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Người phê duyệt (Manager). Null nếu chưa duyệt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
