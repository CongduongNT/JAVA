package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question.Difficulty;
import com.planbookai.backend.model.entity.Question.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * QuestionResponse – Response object đại diện cho 1 câu hỏi.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {

    private Long id;

    private Long bankId; // ✅ FIX Integer -> Long
    private String bankName;

    private Long createdById;
    private String createdByName;

    private String content;

    private QuestionType type; // ✅ FIX String -> Enum
    private Difficulty difficulty; // ✅ FIX String -> Enum

    private String topic;

    private List<Map<String, Object>> options;

    private String correctAnswer;

    private String explanation;

    private boolean aiGenerated; // ✅ FIX Boolean -> boolean

    private boolean approved; // ✅ FIX Boolean -> boolean

    private Long approvedById;
    private String approvedByName;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}