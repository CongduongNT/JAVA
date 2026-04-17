package com.planbookai.backend.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class UploadAnswerSheetRequest {

    private Long examId;
    private List<MultipartFile> files;

    public UploadAnswerSheetRequest() {
    }

    public UploadAnswerSheetRequest(Long examId, List<MultipartFile> files) {
        this.examId = examId;
        this.files = files;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartFile> files) {
        this.files = files;
    }
}
