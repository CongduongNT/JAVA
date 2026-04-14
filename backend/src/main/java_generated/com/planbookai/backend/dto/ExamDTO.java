package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ExamDTO – Response object đại diện cho 1 đề thi.
 *
 * <p>Dùng cho cả:
 * <ul>
 *   <li>GET /exams/{id} – xem chi tiết</li>
 *   <li>POST /exams/ai-generate – kết quả sinh đề</li>
 *   <li>GET /exams – danh sách đề của teacher</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDTO {

    private Long id;

    /** ID giáo viên tạo đề. */
    private Long teacherId;

    /** Tên giáo viên tạo đề. */
    private String teacherName;

    private String title;
    private String subject;
    private String gradeLevel;
    private String topic;

    /** Tổng số câu hỏi trong đề. */
    private Integer totalQuestions;

    /** Thời gian làm bài (phút). */
    private Integer durationMins;

    /** Có trộn thứ tự câu hỏi không. */
    private Boolean randomized;

    /** Số phiên bản đề. */
    private Integer versionCount;

    /** Trạng thái: DRAFT | PUBLISHED | CLOSED. */
    private String status;

    /** true nếu đề được sinh bởi AI. */
    private Boolean aiGenerated;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Danh sách câu hỏi trong đề (chỉ trả về khi xem chi tiết).
     * Null khi xem danh sách.
     */
    private List<ExamQuestionDTO> questions;

    // -----------------------------------------------------------------------
    // Nested DTO
    // -----------------------------------------------------------------------

    /**
     * ExamQuestionDTO – câu hỏi bên trong đề thi.
     *
     * <p>Gồm thông tin câu hỏi (QuestionDTO) + số thứ tự + điểm trong đề.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamQuestionDTO {

        /** ID bản ghi exam_questions (junction table). */
        private Long examQuestionId;

        /** Thứ tự câu hỏi trong đề (1-indexed). */
        private Integer orderIndex;

        /** Phiên bản đề (thường 1 trừ khi có đề nhiều mã). */
        private Integer versionNumber;

        /** Điểm của câu này trong đề. */
        private java.math.BigDecimal points;

        /** Nguồn gốc câu hỏi: "BANK" nếu lấy từ bank, "AI" nếu sinh mới. */
        private String source;

        /** Câu hỏi đầy đủ. */
        private QuestionDTO question;
    }
}
