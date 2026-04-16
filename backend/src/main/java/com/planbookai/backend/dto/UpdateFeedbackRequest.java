package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateFeedbackRequest – Body cho PUT /api/v1/grading-results/{id}/feedback
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackRequest {

    /**
     * Nhận xét của giáo viên. Null = giữ nguyên feedback cũ.
     */
    private String teacherFeedback;

    /**
     * true = gọi AI sinh gợi ý feedback.
     * AI suggestion được trả về trong response.
     * Không tự động lưu – teacher phải POST lại với teacherFeedback.
     */
    private boolean requestAiFeedback;
}
