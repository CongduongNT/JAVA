package com.planbookai.backend.dto;

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
public class SavePreviewedQuestionsRequest {

    private Integer bankId;

    private List<QuestionDTO> questions;

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
}
