package com.planbookai.backend.service;

import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.UploadAnswerSheetRequest;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.mapper.AnswerSheetMapper;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import com.planbookai.backend.repository.ExamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        assertTeacher(user, "Only teacher can upload answer sheets");

        Long examId = request != null ? request.getExamId() : null;
        if (examId == null) {
            throw new IllegalArgumentException("examId is required");
        }

        List<MultipartFile> files = request != null ? request.getFiles() : null;
        validateFiles(files);

        Exam exam = findOwnedExam(examId, user, "You do not have permission to upload answer sheets for this exam");

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
                .map(AnswerSheetMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AnswerSheetDTO> getMyAnswerSheets(User user, Integer page, Integer size, Long examId) {
        assertTeacher(user, "Only teacher can access answer sheets");
        validatePageAndSize(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "uploadedAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<AnswerSheet> answerSheetPage;
        if (examId != null) {
            findOwnedExam(examId, user, "You do not have permission to access answer sheets for this exam");
            answerSheetPage = answerSheetRepository.findByTeacher_IdAndExam_Id(user.getId(), examId, pageable);
        } else {
            answerSheetPage = answerSheetRepository.findByTeacher_Id(user.getId(), pageable);
        }

        return PageResponse.from(answerSheetPage.map(AnswerSheetMapper::toDTO));
    }

    @Transactional(readOnly = true)
    public AnswerSheetDTO getAnswerSheet(Long id, User user) {
        assertTeacher(user, "Only teacher can access answer sheets");
        AnswerSheet answerSheet = answerSheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Answer sheet not found: " + id));
        assertOwnsAnswerSheet(answerSheet, user);
        return AnswerSheetMapper.toDTO(answerSheet);
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

    private void assertTeacher(User user, String forbiddenMessage) {
        requireAuthenticatedUser(user);
        if (hasRole(user, Role.RoleName.TEACHER)) {
            return;
        }
        throw new ForbiddenOperationException(forbiddenMessage);
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

    private void assertOwnsExam(Exam exam, User user, String forbiddenMessage) {
        Long teacherId = exam.getTeacher() != null ? exam.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException(forbiddenMessage);
    }

    private Exam findOwnedExam(Long examId, User user, String forbiddenMessage) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));
        assertOwnsExam(exam, user, forbiddenMessage);
        return exam;
    }

    private void assertOwnsAnswerSheet(AnswerSheet answerSheet, User user) {
        Long teacherId = answerSheet.getTeacher() != null ? answerSheet.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to access this answer sheet");
    }

    private void validatePageAndSize(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size == null || size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("size must not exceed 100");
        }
    }

    private String buildStorageFolder(Long teacherId, Long examId) {
        return "answer-sheets/" + teacherId + "/exam-" + examId;
    }
}
