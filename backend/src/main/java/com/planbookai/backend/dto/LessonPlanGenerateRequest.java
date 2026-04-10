package com.planbookai.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LessonPlanGenerateRequest – Body của API sinh giáo án bằng AI.
 *
 * <pre>
 * {
 *   "subject": "Toán",
 *   "gradeLevel": "Lớp 4",
 *   "topic": "Phép cộng phân số",
 *   "objectives": ["Hiểu khái niệm phân số", "Cộng được hai phân số cùng mẫu"],
 *   "durationMinutes": 45,
 *   "framework": "E5"
 * }
 * </pre>
 */
@Data
public class LessonPlanGenerateRequest {

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "gradeLevel is required")
    private String gradeLevel;

    @NotBlank(message = "topic is required")
    private String topic;

    /** Danh sách mục tiêu bài học. Nếu rỗng, AI sẽ tự suy ra. */
    private String objectives;

    @Min(value = 15, message = "durationMinutes must be at least 15")
    @Max(value = 180, message = "durationMinutes must not exceed 180")
    private int durationMinutes = 45;

    /**
     * Framework giảng dạy: E5 | E3 | E4 | BACKWARD_DESIGN | TGAP.
     * Mặc định E5.
     */
    private String framework = "E5";

    /**
     * Nếu true → sinh và lưu vào DB, trả về LessonPlanDTO có id.
     * Nếu false → chỉ sinh preview, không lưu DB.
     * Mặc định false.
     */
    private boolean saveToDb = false;
}
