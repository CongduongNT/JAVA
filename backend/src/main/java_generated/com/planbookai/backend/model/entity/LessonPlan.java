package com.planbookai.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "framework_id")
    private Integer frameworkId;

    @Column(name = "framework_code")
    private String frameworkCode;

    @Column(nullable = false)
    private String title;

    private String subject;

    @Column(name = "grade_level")
    private String gradeLevel;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(columnDefinition = "TEXT")
    private String assessment;

    @Column(columnDefinition = "TEXT")
    private String materials;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "ai_objectives_json", columnDefinition = "TEXT")
    private String aiObjectivesJson;

    @Column(name = "ai_materials_json", columnDefinition = "TEXT")
    private String aiMaterialsJson;

    @Column(name = "lesson_flow_json", columnDefinition = "TEXT")
    private String lessonFlowJson;

    @Column(name = "assessment_json", columnDefinition = "TEXT")
    private String assessmentJson;

    @Column(columnDefinition = "TEXT")
    private String homework;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ai_generated")
    @Builder.Default
    private Boolean aiGenerated = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LessonPlanStatus status = LessonPlanStatus.DRAFT;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LessonPlanStatus {
        DRAFT,
        PUBLISHED
    }
}
