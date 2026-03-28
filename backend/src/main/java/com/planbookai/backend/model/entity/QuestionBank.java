package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * QuestionBank – Ngân hàng câu hỏi (nhóm các câu hỏi theo môn/chủ đề).
 *
 * <p>Mỗi ngân hàng có thể chứa nhiều {@link Question}. Được tạo bởi Staff hoặc Teacher.
 */
@Entity
@Table(name = "question_bank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String subject;

    @Column(name = "grade_level")
    private String gradeLevel;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
