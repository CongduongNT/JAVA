package com.planbookai.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SavePreviewedQuestionsRequest {

    private Integer bankId;
    private List<QuestionDTO> questions;
}
