package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuestionCreateRequest {

    @NotNull(message = "bankId is required")
    private Integer bankId;

    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "type is required")
    private Question.QuestionType type;

    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;
    private String topic;
    private List<Map<String, Object>> options;
    private String correctAnswer;
    private String explanation;
}
