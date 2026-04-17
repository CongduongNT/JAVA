package com.planbookai.backend.service;

import com.planbookai.backend.model.entity.Question;

import java.util.Locale;

/**
 * AnswerNormalizer – Chuẩn hóa đáp án OCR và đáp án đúng trước khi so sánh.
 *
 * <h3>BR-03, BR-04, BR-05, BR-06</h3>
 *
 * <h4>MULTIPLE_CHOICE:</h4>
 * "A", "a", "a)", "Đáp án A", "A.", "dap an a" → "A"<br>
 * Lấy ký tự alphabet đầu tiên, uppercase.
 *
 * <h4>SHORT_ANSWER / FILL_IN_BLANK:</h4>
 * Trim → lowercase → collapse multiple spaces → so sánh exact.
 */
public final class AnswerNormalizer {

    private AnswerNormalizer() {}

    /**
     * Normalize đáp án OCR.
     *
     * @param ocrText raw text từ OCR (có thể null, blank)
     * @param type    loại câu hỏi
     * @return normalized string
     */
    public static String normalize(String ocrText, Question.QuestionType type) {
        if (ocrText == null) return "";

        String trimmed = ocrText.trim();
        if (trimmed.isEmpty()) return "";

        return switch (type) {
            case MULTIPLE_CHOICE -> normalizeMultipleChoice(trimmed);
            case SHORT_ANSWER, FILL_IN_BLANK -> normalizeFreeText(trimmed);
        };
    }

    /**
     * Normalize đáp án đúng từ DB (cùng logic để so sánh).
     */
    public static String normalizeCorrectAnswer(String correctAnswer, Question.QuestionType type) {
        if (correctAnswer == null) return "";

        String trimmed = correctAnswer.trim();
        if (trimmed.isEmpty()) return "";

        return switch (type) {
            case MULTIPLE_CHOICE -> normalizeMultipleChoice(trimmed);
            case SHORT_ANSWER, FILL_IN_BLANK -> normalizeFreeText(trimmed);
        };
    }

    /**
     * MULTIPLE_CHOICE: trích ký tự alphabet đầu tiên, uppercase.
     * Strip prefixes như "Đáp án", "Answer", "a)", "A.", spaces, etc.
     */
    private static String normalizeMultipleChoice(String text) {
        // Strip leading non-alpha characters
        String stripped = text.replaceAll("^[^a-zA-ZÀ-ỹ]+", "");
        if (stripped.isEmpty()) return "";
        return stripped.substring(0, 1).toUpperCase(Locale.forLanguageTag("vi"));
    }

    /**
     * FREE TEXT: trim, lowercase, collapse multiple spaces.
     */
    private static String normalizeFreeText(String text) {
        return text
                .toLowerCase(Locale.forLanguageTag("vi"))
                .replaceAll("\\s+", " ")  // collapse multiple spaces/newlines
                .trim();
    }
}
