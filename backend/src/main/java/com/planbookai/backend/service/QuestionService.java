package com.planbookai.backend.service;

import com.planbookai.backend.dto.QuestionRequest;
import com.planbookai.backend.dto.QuestionResponse;
import com.planbookai.backend.model.entity.Questions;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.QuestionsRepository;
import com.planbookai.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
                .correctAnswer(request.getCorrectAnswer())
                .explanation(request.getExplanation())
                .aiGenerated(request.getAiGenerated() != null ? request.getAiGenerated() : false)
                .isApproved(request.getIsApproved() != null ? request.getIsApproved() : false)
                .build();

        if (request.getOptions() != null) {
            question.setOptions(request.getOptions());
        }

        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getCreatedBy()));
        question.setCreatedBy(creator);

        if (request.getApprovedBy() != null) {
            User approver = userRepository.findById(request.getApprovedBy())
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found with ID: " + request.getApprovedBy()));
            question.setApprovedBy(approver);
        }

        Questions saved = questionsRepository.save(question);
        return mapToResponse(saved);
    }

    @Transactional
    public Optional<QuestionResponse> approve(Long questionId, Long approverId) {
        return questionsRepository.findById(questionId).map(question -> {
            User approver = userRepository.findById(approverId)
                    .orElseThrow(() -> new IllegalArgumentException("Approver not found with ID: " + approverId));
            question.setApprovedBy(approver);
            question.setIsApproved(true);
            Questions saved = questionsRepository.save(question);
            return mapToResponse(saved);
        });
    }

    public java.util.List<QuestionResponse> getQuestions(Boolean approved) {
        java.util.List<Questions> questions;
        if (approved != null) {
            questions = questionsRepository.findByIsApproved(approved);
        } else {
            questions = questionsRepository.findAll();
        }
        return questions.stream().map(this::mapToResponse).toList();
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
                .approvedBy(question.getApprovedBy() != null ? question.getApprovedBy().getId() : null)
                .createdBy(question.getCreatedBy().getId())
                .build();
    }
}