package com.planbookai.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * StudentAnalyticsDTO – Response của API GET /api/v1/analytics/students.
 *
 * <p>Cung cấp tổng quan về các nhóm học sinh mục tiêu của một giáo viên,
 * được phân tích từ cấu trúc các đề thi mà teacher đã tạo.
 *
 * <p>Vì hệ thống hiện chưa có entity {@code Student} (Role chỉ gồm
 * ADMIN / MANAGER / STAFF / TEACHER), analytics được tổng hợp theo
 * chiều <strong>gradeLevel × subject</strong> – mỗi cặp ứng với một
 * "nhóm học sinh" tiêu biểu mà teacher đang nhắm tới.
 *
 * <p>Khi hệ thống bổ sung bảng {@code exam_submissions}, các trường
 * ước lượng ({@code estimatedAvgScore}, {@code estimatedPassRate},
 * {@code scoreDistribution}) sẽ được thay bằng số liệu thực tế.
 */
public record StudentAnalyticsDTO(

        // ── Teacher meta ──────────────────────────────────────────────────────
        Long   teacherId,
        String teacherName,
        String teacherEmail,

        // ── Tổng quan của teacher ─────────────────────────────────────────────
        TeacherSummaryDTO summary,

        // ── Nhóm học sinh theo (gradeLevel × subject) ─────────────────────────
        /** Mỗi phần tử = một nhóm HS mục tiêu (ứng với một khối + môn). */
        List<StudentGroupDTO> studentGroups,

        // ── Breakdown aggregates ──────────────────────────────────────────────
        /** Số đề thi theo từng môn học: { "Hóa học": 8, "Toán": 7 } */
        Map<String, Long> examsBySubject,

        /** Số đề thi theo khối lớp:   { "10": 5, "11": 6, "12": 4 } */
        Map<String, Long> examsByGrade,

        /** Số câu theo độ khó trên toàn bộ đề: { "EASY": 90, "MEDIUM": 150, "HARD": 60 } */
        Map<String, Long> questionsByDifficulty,

        /** Top 5 chủ đề xuất hiện nhiều nhất trong đề thi của teacher. */
        List<TopicFrequencyDTO> topTopics
) {

    // ── Nested records ────────────────────────────────────────────────────────

    /** Tổng quan hoạt động của teacher. */
    public record TeacherSummaryDTO(
            long totalExams,
            long publishedExams,
            long draftExams,
            long totalQuestions,
            long totalAiQuestions,
            long totalBankQuestions,
            long totalQuestionBanks,
            /** Số nhóm HS khác nhau mà teacher đang phụ trách (grade × subject). */
            int  studentGroupCount
    ) {}

    /**
     * Một nhóm học sinh mục tiêu (phân loại theo gradeLevel × subject).
     * Mỗi group ứng với tập hợp đề thi cùng môn + khối.
     */
    public record StudentGroupDTO(
            /** Khối lớp: "6", "7", …, "12" */
            String gradeLevel,

            /** Môn học */
            String subject,

            /** Số đề thi cho nhóm này */
            int examCount,

            /** Số đề đã publish */
            int publishedExamCount,

            /** Tổng số câu hỏi trong tất cả đề của nhóm */
            int totalQuestions,

            /** Điểm trung bình ước tính của nhóm (thang 10) */
            double estimatedAvgScore,

            /** Tỉ lệ đạt ước tính (%) */
            double estimatedPassRate,

            /** Phân bổ độ khó: { EASY, MEDIUM, HARD } */
            Map<String, Long> difficultyBreakdown,

            /** Tỉ lệ câu AI/Bank trong nhóm đề này */
            double aiQuestionRatio,

            /** Danh sách chủ đề (topic) đã ra trong nhóm */
            List<String> topics,

            /** Danh sách đề thi gần nhất của nhóm (tối đa 5) */
            List<RecentExamDTO> recentExams
    ) {}

    /** Thông tin tóm tắt một đề thi trong danh sách gần đây. */
    public record RecentExamDTO(
            Long   examId,
            String title,
            String status,
            int    totalQuestions,
            double estimatedAvgScore,
            double estimatedPassRate,
            LocalDateTime createdAt
    ) {}

    /** Tần suất xuất hiện của một chủ đề. */
    public record TopicFrequencyDTO(
            String topic,
            long   examCount,
            long   questionCount
    ) {}
}
