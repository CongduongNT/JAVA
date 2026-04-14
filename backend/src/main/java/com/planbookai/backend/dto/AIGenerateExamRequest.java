package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.Question;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * AIGenerateExamRequest – Body của API POST /api/v1/exams/ai-generate.
 *
 * <p>Teacher gửi request này để tạo đề thi tự động.
 * Hệ thống sẽ:
 * <ol>
 *   <li>Lấy câu hỏi sẵn có từ question bank (lọc theo môn, chủ đề, độ khó).</li>
 *   <li>Nếu số câu từ bank chưa đủ {@code totalQuestions}, sinh thêm bằng Gemini AI.</li>
 *   <li>Gộp lại thành đề và lưu vào DB.</li>
 * </ol>
 *
 * <pre>
 * {
 *   "title": "Kiểm tra Hóa học - Chương 1",
 *   "subject": "Chemistry",
 *   "gradeLevel": "10",
 *   "topic": "Nguyên tử - Phân tử",
 *   "totalQuestions": 20,
 *   "durationMins": 45,
 *   "difficulty": "MEDIUM",
 *   "questionType": "MULTIPLE_CHOICE",
 *   "bankIds": [1, 2],
 *   "randomized": true
 * }
 * </pre>
 */
@Data
public class AIGenerateExamRequest {

    /** Tiêu đề đề thi. */
    @NotBlank(message = "title is required")
    private String title;

    /** Môn học (VD: Chemistry). */
    @NotBlank(message = "subject is required")
    private String subject;

    /** Khối lớp (VD: 10, 11, 12). */
    private String gradeLevel;

    /** Chủ đề / chương cụ thể. */
    @NotBlank(message = "topic is required")
    private String topic;

    /**
     * Tổng số câu hỏi cần có trong đề (1–50).
     * Hệ thống lấy từ bank trước, thiếu thì AI sinh bù.
     */
    @Min(value = 1, message = "totalQuestions must be at least 1")
    @Max(value = 50, message = "totalQuestions must not exceed 50")
    private int totalQuestions = 20;

    /** Thời gian làm bài (phút). */
    @Min(value = 1, message = "durationMins must be positive")
    private Integer durationMins;

    /** Độ khó mong muốn. Mặc định MEDIUM. */
    private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

    /** Loại câu hỏi ưu tiên. Mặc định MULTIPLE_CHOICE. */
    private Question.QuestionType questionType = Question.QuestionType.MULTIPLE_CHOICE;

    /**
     * Danh sách ID ngân hàng câu hỏi để tìm câu sẵn có.
     * Nếu rỗng/null, hệ thống bỏ qua bước tìm từ bank và để AI sinh toàn bộ.
     */
    private List<Integer> bankIds;

    /**
     * Có trộn ngẫu nhiên thứ tự câu hỏi không.
     * Mặc định false.
     */
    private boolean randomized = false;
}
