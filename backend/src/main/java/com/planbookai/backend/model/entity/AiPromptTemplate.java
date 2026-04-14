package com.planbookai.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_prompt_templates")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiPromptTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String purpose; // Ví dụ: QUESTION_GEN, LESSON_PLAN

    @Column(columnDefinition = "TEXT")
    private String promptText;

    private String variables; // Ví dụ: "topic, level"

    private String status; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    
}
