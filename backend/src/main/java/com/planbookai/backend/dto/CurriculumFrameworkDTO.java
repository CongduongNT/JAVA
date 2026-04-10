package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.CurriculumFramework;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurriculumFrameworkDTO {

    private Integer id;
    private String title;
    private String subject;
    private String gradeLevel;
    private String description;
    private String structure;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CreatorDTO createdBy;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorDTO {
        private Long id;
        private String fullName;
        private String email;
    }

    public static CurriculumFrameworkDTO fromEntity(CurriculumFramework framework) {
        CreatorDTO creator = null;
        if (framework.getCreatedBy() != null) {
            creator = CreatorDTO.builder()
                    .id(framework.getCreatedBy().getId())
                    .fullName(framework.getCreatedBy().getFullName())
                    .email(framework.getCreatedBy().getEmail())
                    .build();
        }

        return CurriculumFrameworkDTO.builder()
                .id(framework.getId())
                .title(framework.getTitle())
                .subject(framework.getSubject())
                .gradeLevel(framework.getGradeLevel())
                .description(framework.getDescription())
                .structure(framework.getStructure())
                .isPublished(framework.getIsPublished())
                .createdAt(framework.getCreatedAt())
                .updatedAt(framework.getUpdatedAt())
                .createdBy(creator)
                .build();
    }
}
