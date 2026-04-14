package com.planbookai.backend.service;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import com.planbookai.backend.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnswerSheetService {

    private final AnswerSheetRepository answerSheetRepository;
    private final ExamRepository examRepository;
    private final StorageService storageService;

    public AnswerSheetService(
            AnswerSheetRepository answerSheetRepository,
            ExamRepository examRepository,
            StorageService storageService) {
        this.answerSheetRepository = answerSheetRepository;
        this.examRepository = examRepository;
        this.storageService = storageService;
    }

    @Transactional
    public List<AnswerSheetDTO> uploadAnswerSheets(UploadAnswerSheetRequest request, User user) {
        assertTeacher(user);

        Long examId = request != null ? request.getExamId() : null;
        if (examId == null) {
            throw new IllegalArgumentException("examId is required");
        }

        List<MultipartFile> files = request != null ? request.getFiles() : null;
        validateFiles(files);

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        assertOwnsExam(exam, user);

        String folder = buildStorageFolder(user.getId(), examId);
        List<AnswerSheet> answerSheets = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileUrl = storageService.uploadFile(folder, file);
            AnswerSheet answerSheet = new AnswerSheet();
            answerSheet.setExam(exam);
            answerSheet.setTeacher(user);
            answerSheet.setFileUrl(fileUrl);
            answerSheet.setOcrStatus(AnswerSheet.OcrStatus.PENDING);
            answerSheets.add(answerSheet);
        }

        return answerSheetRepository.saveAll(answerSheets).stream()
                .map(this::mapToDTO)
                .toList();
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("files is required");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("files must not contain empty file");
            }
        }
    }

    private void assertTeacher(User user) {
        requireAuthenticatedUser(user);
        if (hasRole(user, Role.RoleName.TEACHER)) {
            return;
        }
        throw new ForbiddenOperationException("Only teacher can upload answer sheets");
    }

    private void requireAuthenticatedUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ForbiddenOperationException("Authentication is required");
        }
    }

    private boolean hasRole(User user, Role.RoleName roleName) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() == roleName;
    }

    private void assertOwnsExam(Exam exam, User user) {
        Long teacherId = exam.getTeacher() != null ? exam.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to upload answer sheets for this exam");
    }

    private String buildStorageFolder(Long teacherId, Long examId) {
        return "answer-sheets/" + teacherId + "/exam-" + examId;
    }

    private AnswerSheetDTO mapToDTO(AnswerSheet answerSheet) {
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
