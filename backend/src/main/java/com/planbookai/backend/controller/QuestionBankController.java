package com.planbookai.backend.controller;

import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.QuestionBankRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * QuestionBankController – REST API quản lý ngân hàng câu hỏi.
 *
 * <p>Base URL: /api/v1/question-banks
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /question-banks          – Lấy ngân hàng của người dùng hiện tại</li>
 *   <li>GET  /question-banks/{id}     – Lấy chi tiết ngân hàng</li>
 *   <li>POST /question-banks          – Tạo ngân hàng mới</li>
 *   <li>PUT  /question-banks/{id}     – Cập nhật ngân hàng</li>
 *   <li>DELETE /question-banks/{id}   – Xóa ngân hàng</li>
 *   <li>GET  /question-banks/{id}/questions – Lấy câu hỏi trong ngân hàng</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/question-banks")
public class QuestionBankController {

    private final QuestionService questionService;

    public QuestionBankController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Lấy danh sách ngân hàng câu hỏi của người dùng đang đăng nhập.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<List<QuestionDTO.QuestionBankDTO>> getMyBanks(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getMyBanks(user));
    }

    /**
     * Lấy chi tiết một ngân hàng câu hỏi.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> getBank(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.getBank(id, user));
    }

    /**
     * Tạo ngân hàng câu hỏi mới.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> createBank(
            @Valid @RequestBody QuestionBankRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(questionService.createBank(request, user));
    }

    /**
     * Cập nhật thông tin ngân hàng câu hỏi.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<QuestionDTO.QuestionBankDTO> updateBank(
            @PathVariable Integer id,
            @Valid @RequestBody QuestionBankRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(questionService.updateBank(id, request, user));
    }

    /**
     * Xóa ngân hàng câu hỏi (và toàn bộ câu hỏi bên trong).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF')")
    public ResponseEntity<Void> deleteBank(
            @PathVariable Integer id,
            @AuthenticationPrincipal User user) {
        questionService.deleteBank(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách câu hỏi trong một ngân hàng cụ thể.
     */
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
