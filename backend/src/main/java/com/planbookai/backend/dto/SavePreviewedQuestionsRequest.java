package com.planbookai.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class SavePreviewedQuestionsRequest {

    private Integer bankId;
    private List<QuestionDTO> questions;
}
