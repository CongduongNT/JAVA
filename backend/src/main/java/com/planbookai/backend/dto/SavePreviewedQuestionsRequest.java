package com.planbookai.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * SavePreviewedQuestionsRequest – Body của API POST /api/v1/questions/save-batch.
 *
 * <p>Giữ nguyên shape payload từ frontend:
 * <pre>
 * {
 *   "bankId": 5,
 *   "questions": [ { "content": "...", "type": "MULTIPLE_CHOICE", ... } ]
 * }
 * </pre>
 */
@Data
public class SavePreviewedQuestionsRequest {

    private Integer bankId;

    private List<QuestionDTO> questions;
}
