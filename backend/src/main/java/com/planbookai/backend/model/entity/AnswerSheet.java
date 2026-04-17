package com.planbookai.backend.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "answer_sheets")
public class AnswerSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_code")
    private String studentCode;

    @Column(name = "file_url", nullable = false, length = 512)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "ocr_status", nullable = false)
    private OcrStatus ocrStatus = OcrStatus.PENDING;

    @Column(name = "ocr_raw_data", columnDefinition = "json")
    private String ocrRawData;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public enum OcrStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public AnswerSheet() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
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

    public OcrStatus getOcrStatus() {
        return ocrStatus;
    }

    public void setOcrStatus(OcrStatus ocrStatus) {
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
