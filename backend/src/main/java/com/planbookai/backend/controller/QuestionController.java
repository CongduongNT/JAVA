package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AIGenerateQuestionsRequest;
import com.planbookai.backend.dto.QuestionCreateRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.dto.QuestionUpdateRequest;
import com.planbookai.backend.dto.SavePreviewedQuestionsRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO> createQuestion(
            @Valid @RequestBody QuestionCreateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(questionService.createQuestion(request, user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<QuestionDTO> getQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getQuestionById(id, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        questionService.deleteQuestion(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<List<QuestionDTO>> aiGenerateQuestions(
            @Valid @RequestBody AIGenerateQuestionsRequest request,
            @AuthenticationPrincipal User user) {
        List<QuestionDTO> result = questionService.aiGenerateQuestions(request, user);
        return request.isSaveToDb()
                ? ResponseEntity.status(201).body(result)
                : ResponseEntity.ok(result);
    }

    @PostMapping("/save-batch")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<List<QuestionDTO>> savePreviewedQuestions(
            @RequestBody SavePreviewedQuestionsRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201)
                .body(questionService.savePreviewedQuestions(
                        request.getBankId(),
                        request.getQuestions(),
                        user));
    }
}
