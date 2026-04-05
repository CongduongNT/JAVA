package com.planbookai.backend.dto;

import java.util.List;
import com.planbookai.backend.model.entity.Questions.QuestionDifficulty;
import com.planbookai.backend.model.entity.Questions.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionRequest {

    @NotNull
    private Long createdBy;

    @NotBlank
    private String content;

    @NotNull
    private QuestionType type;

    private QuestionDifficulty difficulty;

    private String topic;

    private List<String> options;

    private String correctAnswer;

    private String explanation;

    private Boolean aiGenerated;

    private Boolean isApproved;

    private Long approvedBy;

    
}

