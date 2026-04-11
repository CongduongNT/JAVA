package com.planbookai.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * SaveLessonPlanRequest – Body của API lưu giáo án đã chỉnh sửa.
 *
 * <pre>
 * {
 *   "subject": "Toán",
 *   "gradeLevel": "Lớp 4",
 *   "topic": "Phép cộng phân số",
 *   "objectives": "Mô tả mục tiêu...",
 *   "durationMinutes": 45,
 *   "framework": "E5",
 *   "title": "Bài 5: Phép cộng phân số",
 *   "materials": ["Sách giáo khoa", "Bảng phụ"],
 *   "lessonPlanObjectives": ["Hiểu khái niệm", "Cộng được hai phân số"],
 *   "lessonFlow": [...],
 *   "assessment": { "methods": [...], "criteria": "..." },
 *   "homework": "Làm bài tập 1-3",
 *   "notes": "..."
 * }
 * </pre>
 */
@Data
public class SaveLessonPlanRequest {

    @NotBlank(message = "subject is required")
    private String subject;

    @NotBlank(message = "gradeLevel is required")
    private String gradeLevel;

    @NotBlank(message = "topic is required")
    private String topic;

    private String objectives;

    @Min(value = 15, message = "durationMinutes must be at least 15")
    private int durationMinutes = 45;

    private String framework = "E5";

    // --- Lesson plan fields (từ editor) ---
    private String title;
    private List<String> materials;
    private List<String> lessonPlanObjectives; // objectives trong lesson plan
    private List<LessonPhaseRequest> lessonFlow;
    private AssessmentRequest assessment;
    private String homework;
    private String notes;

    @Data
    public static class LessonPhaseRequest {
        private String phase;
        private int timeMinutes;
        private String activities;
        private String teacherActions;
        private String studentActions;
    }

    @Data
    public static class AssessmentRequest {
        private List<String> methods;
        private String criteria;
    }
}
