package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionResponse {
    private Long id;
    private QuestionDTO question;
    private Integer orderIndex;
    private BigDecimal points;
}