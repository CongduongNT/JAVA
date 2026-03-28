package com.planbookai.backend.util;

import com.planbookai.backend.model.entity.Question;
import org.springframework.stereotype.Component;

/**
 * PromptBuilder – Xây dựng prompt gửi lên Gemini AI để sinh câu hỏi.
 *
 * <p>Thiết kế theo nguyên tắc Single Responsibility: chỉ chịu trách nhiệm
 * xây dựng chuỗi prompt, không phụ thuộc vào bất kỳ service AI nào.
 *
 * <p>Prompt được thiết kế để:
 * <ul>
 *   <li>Buộc Gemini trả về JSON thuần túy (không có markdown code fence).</li>
 *   <li>Định nghĩa rõ schema JSON để dễ parse.</li>
 *   <li>Điều chỉnh theo loại câu hỏi (MULTIPLE_CHOICE / SHORT_ANSWER / FILL_IN_BLANK).</li>
 * </ul>
 *
 * <p>Ví dụ prompt output:
 * <pre>
 * You are an expert Vietnamese high school Chemistry teacher...
 * Generate exactly 5 MULTIPLE_CHOICE questions about "Periodic Table" (difficulty: MEDIUM).
 * Return ONLY a valid JSON array, no markdown, no explanation.
 * ...
 * </pre>
 */
@Component
public class PromptBuilder {

    /**
     * Tạo prompt hoàn chỉnh để gửi lên Gemini AI.
     *
     * @param subject    Môn học (VD: Chemistry)
     * @param topic      Chủ đề (VD: Periodic Table)
     * @param difficulty Độ khó (EASY | MEDIUM | HARD)
     * @param type       Loại câu hỏi
     * @param count      Số câu cần sinh
     * @return Chuỗi prompt hoàn chỉnh
     */
    public String buildQuestionPrompt(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int count) {

        String difficultyLabel = mapDifficultyLabel(difficulty);
        String typeInstructions = buildTypeInstructions(type);

        return """
                You are an expert Vietnamese high school %s teacher and exam writer.
                Your task is to generate exactly %d %s questions about the topic "%s" with %s difficulty.

                %s

                CRITICAL RULES:
                1. Return ONLY a valid JSON array. No markdown, no code fences, no explanation.
                2. The JSON array must contain exactly %d question objects.
                3. All text must be in Vietnamese.
                4. Each question must be educationally accurate and appropriate for high school level.
                5. Difficulty level "%s" means: %s

                Required JSON schema for each question object:
                {
                  "content": "<question text in Vietnamese>",
                  "type": "%s",
                  "difficulty": "%s",
                  "topic": "%s",
                  "options": %s,
                  "correctAnswer": "<correct answer>",
                  "explanation": "<brief explanation in Vietnamese>"
                }

                Generate the JSON array now:
                """.formatted(
                subject, count, type.name(), topic, difficultyLabel,
                typeInstructions,
                count,
                difficultyLabel, getDifficultyDescription(difficulty),
                type.name(), difficulty.name(), topic,
                buildOptionsSchema(type)
        );
    }

    /**
     * Xây dựng hướng dẫn theo loại câu hỏi.
     */
    private String buildTypeInstructions(Question.QuestionType type) {
        return switch (type) {
            case MULTIPLE_CHOICE -> """
                    For MULTIPLE_CHOICE questions:
                    - Provide exactly 4 options labeled A, B, C, D.
                    - Exactly one option must be correct (isCorrect: true), others false.
                    - Options should be plausible but clearly differentiated.""";
            case SHORT_ANSWER -> """
                    For SHORT_ANSWER questions:
                    - The question requires a short text answer (1-3 sentences).
                    - Set options to null.
                    - The correctAnswer field should contain the expected answer.""";
            case FILL_IN_BLANK -> """
                    For FILL_IN_BLANK questions:
                    - Use "___" (three underscores) to indicate the blank in the content.
                    - Set options to null.
                    - The correctAnswer field should contain the word(s) that fill the blank.""";
        };
    }

    /**
     * Schema JSON cho trường options tuỳ từng loại câu hỏi.
     */
    private String buildOptionsSchema(Question.QuestionType type) {
        if (type == Question.QuestionType.MULTIPLE_CHOICE) {
            return """
                    [
                      {"label": "A", "text": "<option A text>", "isCorrect": false},
                      {"label": "B", "text": "<option B text>", "isCorrect": true},
                      {"label": "C", "text": "<option C text>", "isCorrect": false},
                      {"label": "D", "text": "<option D text>", "isCorrect": false}
                    ]""";
        }
        return "null";
    }

    private String mapDifficultyLabel(Question.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "EASY (Dễ)";
            case MEDIUM -> "MEDIUM (Trung bình)";
            case HARD -> "HARD (Khó)";
        };
    }

    private String getDifficultyDescription(Question.Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "recall-level knowledge, straightforward questions";
            case MEDIUM -> "comprehension and application, moderate complexity";
            case HARD -> "analysis and synthesis, complex multi-step reasoning";
        };
    }
}
