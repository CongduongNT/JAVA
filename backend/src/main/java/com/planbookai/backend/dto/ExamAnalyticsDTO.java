package com.planbookai.backend.dto;

import java.util.List;
import java.util.Map;

/**
 * ExamAnalyticsDTO – Response của API GET /api/v1/analytics/exams/{id}/results.
 *
 * <p>Cung cấp thống kê tổng hợp về kết quả một đề thi:
 * <ul>
 *   <li>{@code examId}             – ID đề thi</li>
 *   <li>{@code examTitle}          – Tên đề thi</li>
 *   <li>{@code subject}            – Môn học</li>
 *   <li>{@code gradeLevel}         – Khối lớp</li>
 *   <li>{@code totalQuestions}     – Tổng số câu hỏi trong đề</li>
 *   <li>{@code avgScore}           – Điểm trung bình (0–10, làm tròn 2 chữ số)</li>
 *   <li>{@code passRate}           – Tỉ lệ đạt (%) – mặc định pass khi avgScore >= 5</li>
 *   <li>{@code scoreDistribution}  – Phân phối điểm theo dải (0-2, 3-4, 5-6, 7-8, 9-10)</li>
 *   <li>{@code questionStats}      – Thống kê từng câu hỏi</li>
 *   <li>{@code difficultyStats}    – Phân phối câu theo độ khó</li>
 *   <li>{@code aiVsBankStats}      – Tỉ lệ câu AI vs Bank</li>
 * </ul>
 *
 * <p>Lưu ý: Trong phiên bản hiện tại, hệ thống chưa có bảng {@code exam_submissions}.
 * Analytics được tính dựa trên metadata câu hỏi trong đề thi (cấu trúc đề, độ khó, nguồn gốc).
 * Điểm avg/pass rate được mô phỏng theo phân phối chuẩn dựa trên difficulty_mix.
 */
public record ExamAnalyticsDTO(

        Long examId,
        String examTitle,
        String subject,
        String gradeLevel,
        String topic,
        Integer totalQuestions,
        Integer durationMins,
        String status,

        // ── Core metrics ──────────────────────────────────────────────────────
        /** Điểm trung bình (thang 10). Null nếu chưa có submission. */
        Double avgScore,

        /** Tỉ lệ đạt (0.0 – 100.0). Null nếu chưa có submission. */
        Double passRate,

        // ── Score distribution ────────────────────────────────────────────────
        /**
         * Phân phối điểm số.
         * Key: nhãn dải điểm ("0-2", "3-4", "5-6", "7-8", "9-10")
         * Value: số lượng (hoặc tỉ lệ %) học sinh trong dải đó.
         */
        Map<String, Integer> scoreDistribution,

        // ── Per-question stats ────────────────────────────────────────────────
        List<QuestionStatDTO> questionStats,

        // ── Difficulty breakdown ──────────────────────────────────────────────
        Map<String, Long> difficultyStats,

        // ── AI vs Bank ────────────────────────────────────────────────────────
        AiVsBankDTO aiVsBankStats
) {

    // ── Nested DTOs ───────────────────────────────────────────────────────────

    /**
     * Thống kê một câu hỏi trong đề thi.
     */
    public record QuestionStatDTO(
            Long questionId,
            Integer orderIndex,
            String content,
            String difficulty,
            String type,
            String source,          // "BANK" | "AI"
            String topic,
            Double points,
            /** Tỉ lệ đúng ước tính theo difficulty (EASY=0.80, MEDIUM=0.60, HARD=0.35). */
            Double estimatedCorrectRate
    ) {}

    /**
     * Số câu từ AI và từ Bank trong đề thi.
     */
    public record AiVsBankDTO(
            int bankCount,
            int aiCount,
            double bankRatio,
            double aiRatio
    ) {}
}
