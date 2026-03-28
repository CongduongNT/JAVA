package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AIGenerateQuestionsRequest – Body của API POST /api/v1/questions/ai-generate.
 *
 * <p>Teacher gửi request này để Gemini AI sinh câu hỏi và lưu vào ngân hàng.
 *
 * <pre>
 * {
 *   "bankId": 5,
 *   "subject": "Chemistry",
 *   "topic": "Periodic Table",
 *   "difficulty": "MEDIUM",
 *   "type": "MULTIPLE_CHOICE",
 *   "count": 10
 * }
 * </pre>
 */
@Data
public class AIGenerateQuestionsRequest {

    /** ID của ngân hàng câu hỏi cần lưu các câu hỏi vào. */
    @NotNull(message = "bankId is required")
    private Integer bankId;

    /** Môn học (VD: Chemistry, Math, Biology). */
    @NotBlank(message = "subject is required")
    private String subject;

    /** Chủ đề cụ thể (VD: Periodic Table, Acids and Bases). */
    @NotBlank(message = "topic is required")
    private String topic;

    /** Độ khó: EASY | MEDIUM | HARD. Mặc định MEDIUM. */
    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

    /** Loại câu hỏi: MULTIPLE_CHOICE | SHORT_ANSWER | FILL_IN_BLANK. */
    @NotNull(message = "type is required")
    private Question.QuestionType type;

    /** Số câu hỏi cần sinh (1–20). */
    @Min(value = 1, message = "count must be at least 1")
    @Max(value = 20, message = "count must not exceed 20")
    private int count = 5;

    /**
     * Nếu saveToDb = false, chỉ trả về preview mà KHÔNG lưu vào DB.
     * Mặc định false – FE sẽ dùng endpoint này để preview trước.
     */
    private boolean saveToDb = false;
}
