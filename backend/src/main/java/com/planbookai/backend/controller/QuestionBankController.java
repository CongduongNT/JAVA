package com.planbookai.backend.controller;

import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.QuestionBankRequest;
import com.planbookai.backend.dto.QuestionDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/question-banks")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionService questionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<List<QuestionDTO.QuestionBankDTO>> getMyBanks(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getMyBanks(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> getBank(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getBank(id, user));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> createBank(
            @Valid @RequestBody QuestionBankRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(questionService.createBank(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> updateBank(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionBankRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.updateBank(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<Void> deleteBank(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        questionService.deleteBank(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/questions")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<PageResponse<QuestionDTO>> getQuestionsInBank(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String type,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getQuestionsByBank(id, user, page, size, topic, difficulty, type));
    }
}
