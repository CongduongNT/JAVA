package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question.Difficulty;
import com.planbookai.backend.model.entity.Question.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * QuestionRequest – Body để tạo / cập nhật câu hỏi.
 */
@Data
public class QuestionRequest {

    @NotNull(message = "bankId is required")
    private Long bankId; //  FIX Integer -> Long

    @NotNull(message = "createdBy is required")
    private Long createdBy; // FIX thiếu field

    @NotBlank(message = "content is required")
    private String content;

    @NotNull(message = "type is required")
    private QuestionType type; // FIX String -> Enum

    private Difficulty difficulty; // FIX String -> Enum

    private String topic;

    /**
     * Danh sách đáp án (cho MULTIPLE_CHOICE).
     * Format: [{label: "A", text: "...", isCorrect: true/false}]
     */
    private List<Map<String, Object>> options;

    private String correctAnswer;

    private String explanation;

    private Boolean aiGenerated;
}