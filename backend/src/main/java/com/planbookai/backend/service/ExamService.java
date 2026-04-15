package com.planbookai.backend.service;

import com.planbookai.backend.dto.ExamRequest;
import com.planbookai.backend.dto.ExamQuestionResponse;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.ExamQuestion;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ExamService {
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    public List<Exam> getMyExams(User user) {
        return examRepository.findByCreatedByIdOrderByCreatedAtDesc(user.getId());
    }

    public Exam getExamById(Long id, User user) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        if (!exam.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return exam;
    }

    @Transactional
    public Exam createExam(ExamRequest request, User user) {
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .subject(request.getSubject())
                .gradeLevel(request.getGradeLevel())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .totalPoints(request.getTotalPoints())
                .status(Exam.ExamStatus.valueOf(request.getStatus()))
                .settings(request.getSettings())
                .createdBy(user)
                .build();

        if (request.getQuestions() != null) {
            mapQuestionsToExam(exam, request.getQuestions());
        }

        return examRepository.save(exam);
    }

    @Transactional
    public Exam updateExam(Long id, ExamRequest request, User user) {
        Exam exam = getExamById(id, user);
        
        exam.setTitle(request.getTitle());
        exam.setSubject(request.getSubject());
        exam.setGradeLevel(request.getGradeLevel());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setTotalPoints(request.getTotalPoints());
        exam.setStatus(Exam.ExamStatus.valueOf(request.getStatus()));
        exam.setSettings(request.getSettings());
        exam.setUpdatedAt(LocalDateTime.now());

        // Cập nhật danh sách câu hỏi: Xóa cũ thêm mới (Orphan removal sẽ xử lý)
        exam.getExamQuestions().clear();
        if (request.getQuestions() != null) {
            mapQuestionsToExam(exam, request.getQuestions());
        }

        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long id, User user) {
        Exam exam = getExamById(id, user);
        examRepository.delete(exam);
    }

    @Transactional
    public List<ExamQuestionResponse> removeQuestionFromExam(Long id, Long questionId, User user) {
        Exam exam = getExamById(id, user);
        boolean removed = exam.getExamQuestions().removeIf(eq -> eq.getQuestion().getId().equals(questionId));
        if (removed) {
            exam.setUpdatedAt(LocalDateTime.now());
            examRepository.save(exam);
        }
        return getExamQuestions(id, user);
    }

    @Transactional
    public List<ExamQuestionResponse> addQuestionsToExam(Long id, List<ExamRequest.QuestionItem> items, User user) {
        Exam exam = getExamById(id, user);
        if (items != null && !items.isEmpty()) {
            mapQuestionsToExam(exam, items);
            examRepository.save(exam);
        }
        return getExamQuestions(id, user);
    }

    @Transactional(readOnly = true)
    public List<ExamQuestionResponse> getExamQuestions(Long id, User user) {
        Exam exam = getExamById(id, user);
        return exam.getExamQuestions().stream()
                .map(eq -> ExamQuestionResponse.builder()
                        .id(eq.getId())
                        .question(questionService.mapToQuestionDTO(eq.getQuestion()))
                        .orderIndex(eq.getOrderIndex())
                        .points(eq.getPoints())
                        .build())
                .sorted(Comparator.comparing(ExamQuestionResponse::getOrderIndex))
                .collect(Collectors.toList());
    }

    @Transactional
    public Exam publishExam(Long id, User user) {
        Exam exam = getExamById(id, user);
        exam.setStatus(Exam.ExamStatus.PUBLISHED);
        exam.setUpdatedAt(LocalDateTime.now());
        return examRepository.save(exam);
    }

    private void mapQuestionsToExam(Exam exam, List<ExamRequest.QuestionItem> questionItems) {
        for (ExamRequest.QuestionItem item : questionItems) {
            Question q = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found: " + item.getQuestionId()));
            
            ExamQuestion eq = ExamQuestion.builder()
                    .exam(exam)
                    .question(q)
                    .orderIndex(item.getOrderIndex())
                    .points(item.getPoints())
                    .build();
            
            exam.getExamQuestions().add(eq);
        }
    }
}