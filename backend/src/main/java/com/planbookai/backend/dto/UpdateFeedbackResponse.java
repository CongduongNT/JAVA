package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * UpdateFeedbackResponse – Response sau khi update feedback.
 * Trả về AI suggestion nếu requestAiFeedback = true.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackResponse {

    private Long id;
    private String studentName;
    private String teacherFeedback;
    private String aiFeedbackSuggestion;
    private String feedbackSource;
    private String updatedAt;
}
