package com.planbookai.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Map;

/**
 * AIGenerateExamRequest – Body của API POST /api/v1/exams/ai-generate.
 *
 * <p>Teacher gửi request này để tạo đề thi tự động.
 * Hệ thống sẽ:
 * <ol>
 *   <li>Lấy câu hỏi sẵn có từ question bank (lọc theo môn, chủ đề, độ khó).</li>
 *   <li>Nếu số câu từ bank chưa đủ, sinh thêm bằng Gemini AI.</li>
 *   <li>Gộp lại thành đề và lưu vào DB.</li>
 * </ol>
 *
 * <h2>difficulty_mix</h2>
 * <p>Cho phép quy định số câu theo từng mức độ khó. Ví dụ:
 * <pre>
 * {
 *   "subject":          "Chemistry",
 *   "grade_level":      "10",
 *   "topic":            "Nguyên tử - Phân tử",
 *   "total_questions":  20,
 *   "difficulty_mix":   { "EASY": 5, "MEDIUM": 10, "HARD": 5 },
 *   "bank_id":          1
 * }
 * </pre>
 *
 * <p>Nếu {@code difficulty_mix} được cung cấp, tổng các value phải bằng
 * {@code total_questions}. Nếu bỏ qua, hệ thống dùng {@code difficulty} (default MEDIUM)
 * cho toàn bộ câu.
 *
 * <p>Nếu {@code bank_id} bỏ qua (null), AI sinh toàn bộ câu.
 */
@Data
public class AIGenerateExamRequest {

    // ------------------------------------------------------------------
    // Tiêu đề đề thi (tuỳ chọn – tự động tạo từ subject + topic nếu null)
    // ------------------------------------------------------------------

    /** Tiêu đề đề thi (optional – auto-generated if omitted). */
    private String title;

    // ------------------------------------------------------------------
    // Trường bắt buộc (khớp chính xác với spec API)
    // ------------------------------------------------------------------

    /** Môn học (VD: Chemistry). */
    @NotBlank(message = "subject is required")
    private String subject;

    /** Khối lớp (VD: 10, 11, 12). */
    @JsonProperty("grade_level")
    private String gradeLevel;

    /** Chủ đề / chương cụ thể. */
    @NotBlank(message = "topic is required")
    private String topic;

    /**
     * Tổng số câu hỏi cần có trong đề (1–50).
     * Hệ thống lấy từ bank trước, thiếu thì AI sinh bù.
     */
    @JsonProperty("total_questions")
    @Min(value = 1, message = "total_questions must be at least 1")
    @Max(value = 50, message = "total_questions must not exceed 50")
    private int totalQuestions = 20;

    // ------------------------------------------------------------------
    // Phân bổ độ khó
    // ------------------------------------------------------------------

    /**
     * Phân bổ số câu theo độ khó.
     *
     * <p>Map key là tên enum {@link Question.Difficulty} (EASY | MEDIUM | HARD).
     * Map value là số câu muốn có ở mức đó.
     *
     * <p>Ví dụ: {@code {"EASY": 5, "MEDIUM": 10, "HARD": 5}}
     *
     * <p>Nếu {@code null}, toàn bộ đề dùng {@code difficulty} (default MEDIUM).
     */
    @JsonProperty("difficulty_mix")
    private Map<String, Integer> difficultyMix;

    /**
     * Độ khó mặc định khi không có {@code difficulty_mix}.
     * Mặc định: MEDIUM.
     */
    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

    // ------------------------------------------------------------------
    // Ngân hàng câu hỏi & cấu hình
    // ------------------------------------------------------------------

    /**
     * ID ngân hàng câu hỏi muốn dùng (singular).
     * Nếu null, hệ thống bỏ qua bước tìm từ bank và để AI sinh toàn bộ.
     */
    @JsonProperty("bank_id")
    private Integer bankId;

    /** Loại câu hỏi ưu tiên. Mặc định MULTIPLE_CHOICE. */
    @JsonProperty("question_type")
    private Question.QuestionType questionType = Question.QuestionType.MULTIPLE_CHOICE;

    /** Thời gian làm bài (phút). */
    @JsonProperty("duration_mins")
    @Min(value = 1, message = "duration_mins must be positive")
    private Integer durationMins;

    /** Có trộn ngẫu nhiên thứ tự câu hỏi không. Mặc định false. */
    private boolean randomized = false;

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /**
     * Trả về tiêu đề đề thi, tự động tạo từ subject + topic nếu {@code title} null.
     */
    public String resolvedTitle() {
        if (title != null && !title.isBlank()) return title;
        return "Đề thi %s – %s".formatted(subject, topic);
    }

    /**
     * Kiểm tra request có dùng difficulty_mix hay không.
     */
    public boolean hasDifficultyMix() {
        return difficultyMix != null && !difficultyMix.isEmpty();
    }
}
