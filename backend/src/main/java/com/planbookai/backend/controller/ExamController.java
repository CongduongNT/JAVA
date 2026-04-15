package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ExamRequest;
import com.planbookai.backend.dto.ExamQuestionResponse;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.ExamService;
import com.planbookai.backend.service.UserService; // Giả định bạn có service này để lấy user hiện tại
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    private final UserService userService; // Để lấy User object từ Principal

    @GetMapping
    public ResponseEntity<List<Exam>> getAllMyExams(Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.getMyExams(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.getExamById(id, user));
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody ExamRequest request, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.createExam(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody ExamRequest request, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.updateExam(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        examService.deleteExam(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ExamQuestionResponse>> getExamQuestions(@PathVariable Long id, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.getExamQuestions(id, user));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ExamQuestionResponse>> addQuestions(@PathVariable Long id, @RequestBody List<ExamRequest.QuestionItem> questions, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.addQuestionsToExam(id, questions, user));
    }

    @DeleteMapping("/{id}/questions/{qid}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ExamQuestionResponse>> removeQuestion(@PathVariable Long id, @PathVariable Long qid, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.removeQuestionFromExam(id, qid, user));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Exam> publishExam(@PathVariable Long id, Principal principal) {
        User user = userService.getByEmail(principal.getName());
        return ResponseEntity.ok(examService.publishExam(id, user));
    }
}
