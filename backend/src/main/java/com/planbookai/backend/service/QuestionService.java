package com.planbookai.backend.service;

import com.planbookai.backend.dto.QuestionRequest;
import com.planbookai.backend.dto.QuestionResponse;
import com.planbookai.backend.model.entity.Questions;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.QuestionsRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class QuestionService {

    private final QuestionsRepository questionsRepository;
    private final UserRepository userRepository;

    public QuestionService(QuestionsRepository questionsRepository, UserRepository userRepository) {
        this.questionsRepository = questionsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuestionResponse create(QuestionRequest request) {

        Questions question = Questions.builder()
                .content(request.getContent())
                .type(request.getType())
                .difficulty(request.getDifficulty())
                .topic(request.getTopic())
                .options(request.getOptions()) // FIX
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .aiGenerated(Boolean.TRUE.equals(request.getAiGenerated()))
                .isApproved(Boolean.TRUE.equals(request.getIsApproved()))
                .build();

        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getCreatedBy()));

        question.setCreatedBy(creator);

        if (request.getApprovedBy() != null) {
            User approver = userRepository.findById(request.getApprovedBy())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Approver not found with ID: " + request.getApprovedBy()));

            question.setApprovedBy(approver);
        }

        Questions saved = questionsRepository.save(question);
        return mapToResponse(saved);
    }

    @Transactional
    public QuestionResponse approve(Long questionId, Long approverId) {

        Questions question = questionsRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Question not found with ID: " + questionId));

        if (question.isApproved()) {
            throw new IllegalStateException("Question already approved");
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approver not found with ID: " + approverId));

        question.setApprovedBy(approver);
        question.setApproved(true);

        Questions saved = questionsRepository.save(question);
        return mapToResponse(saved);
    }

    private QuestionResponse mapToResponse(Questions question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .content(question.getContent())
                .type(question.getType())
                .difficulty(question.getDifficulty())
                .topic(question.getTopic())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .aiGenerated(question.isAiGenerated())
                .isApproved(question.isApproved())
                .approvedBy(
                        question.getApprovedBy() != null
                                ? question.getApprovedBy().getId()
                                : null)
                .createdBy(
                        question.getCreatedBy() != null
                                ? question.getCreatedBy().getId()
                                : null)
                .build();
    }

    public List<QuestionResponse> getQuestions(Boolean approved) {

        List<Questions> list;

        if (approved == null) {
            list = questionsRepository.findAll();
        } else {
            list = questionsRepository.findByIsApproved(approved);
        }

        return list.stream()
                .map(this::mapToResponse)
                .toList();
    }
}