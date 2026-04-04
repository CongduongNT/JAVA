package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ErrorResponse;
import com.planbookai.backend.dto.QuestionResponse;
import com.planbookai.backend.service.CurrentUserService;
import com.planbookai.backend.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final CurrentUserService currentUserService;

    public QuestionController(QuestionService questionService, CurrentUserService currentUserService) {
        this.questionService = questionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Boolean approved) {
        try {
            var questions = questionService.getQuestions(approved);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch questions: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            var currentUser = currentUserService.getCurrentUserEntity();

            if (currentUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Current user not found"));
            }

            QuestionResponse updated = questionService.approve(id, currentUser.get().getId());

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {
            // question không tồn tại hoặc user không tồn tại
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (IllegalStateException e) {
            // đã approve rồi
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to approve question: " + e.getMessage()));
        }
    }

}