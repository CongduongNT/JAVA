package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Questions.QuestionDifficulty;
import com.planbookai.backend.model.entity.Questions.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResponse {

    private Long id;

    private String content;

    private QuestionType type;

    private QuestionDifficulty difficulty;

    private String topic;

    private Object options;

    private String correctAnswer;

    private String explanation;

    private Boolean aiGenerated;

    private Boolean isApproved;

    private Long approvedBy;

    private Long createdBy;
}