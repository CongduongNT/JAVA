package com.planbookai.backend.mapper;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.model.entity.AnswerSheet;

public final class AnswerSheetMapper {

    private AnswerSheetMapper() {
    }

    public static AnswerSheetDTO toDTO(AnswerSheet answerSheet) {
        AnswerSheetDTO dto = new AnswerSheetDTO();
        dto.setId(answerSheet.getId());
        dto.setExamId(answerSheet.getExam() != null ? answerSheet.getExam().getId() : null);
        dto.setTeacherId(answerSheet.getTeacher() != null ? answerSheet.getTeacher().getId() : null);
        dto.setStudentName(answerSheet.getStudentName());
        dto.setStudentCode(answerSheet.getStudentCode());
        dto.setFileUrl(answerSheet.getFileUrl());
        dto.setOcrStatus(answerSheet.getOcrStatus());
        dto.setOcrRawData(answerSheet.getOcrRawData());
        dto.setUploadedAt(answerSheet.getUploadedAt());
        return dto;
    }
}
