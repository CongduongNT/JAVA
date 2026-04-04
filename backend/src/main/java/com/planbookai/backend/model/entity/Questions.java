package com.planbookai.backend.model.entity;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private QuestionDifficulty difficulty = QuestionDifficulty.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Object options;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "ai_generated", nullable = false)
    @Builder.Default
    private boolean aiGenerated = false;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean isApproved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "created_at",insertable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDate updatedAt;

    public enum QuestionType {
        MULTIPLE_CHOICE,
         FILL_IN_BLANK, 
         SHORT_ANSWER
    }
    public enum QuestionDifficulty {
        EASY, 
        MEDIUM, 
        HARD
    }

}