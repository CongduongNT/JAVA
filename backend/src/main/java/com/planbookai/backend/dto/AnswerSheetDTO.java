package com.planbookai.backend.dto;

import com.planbookai.backend.model.entity.AnswerSheet;

import java.time.LocalDateTime;

public class AnswerSheetDTO {

    private Long id;
    private Long examId;
    private Long teacherId;
    private String studentName;
    private String studentCode;
    private String fileUrl;
    private AnswerSheet.OcrStatus ocrStatus;
    private String ocrRawData;
    private LocalDateTime uploadedAt;

    public AnswerSheetDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public AnswerSheet.OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(AnswerSheet.OcrStatus ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    public String getOcrRawData() {
        return ocrRawData;
    }

    public void setOcrRawData(String ocrRawData) {
        this.ocrRawData = ocrRawData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
