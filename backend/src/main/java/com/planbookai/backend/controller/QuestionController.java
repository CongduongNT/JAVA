package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AIGenerateQuestionsRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * QuestionController – REST API quản lý câu hỏi (CRUD + AI Generate).
 *
 * <p>Base URL: /api/v1/questions
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /questions/{id}          – Xem chi tiết 1 câu hỏi</li>
 *   <li>DELETE /questions/{id}          – Xóa câu hỏi</li>
 *   <li>POST   /questions/ai-generate   – Sinh câu hỏi bằng AI (preview hoặc lưu)</li>
 *   <li>POST   /questions/save-batch    – Lưu danh sách câu hỏi đã preview/chỉnh sửa</li>
 * </ul>
 *
 * <h2>Luồng AI Generate (2 bước):</h2>
 * <ol>
 *   <li>FE gọi POST /questions/ai-generate với {@code saveToDb=false} → nhận preview.</li>
 *   <li>Sau khi user review/chỉnh sửa, FE gọi POST /questions/save-batch → lưu vào DB.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Questions", description = "Quản lý câu hỏi và sinh câu hỏi bằng AI")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Lấy chi tiết câu hỏi theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết câu hỏi")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    /**
     * Xóa câu hỏi theo ID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(summary = "Xóa câu hỏi")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * [AI GENERATE] Sinh câu hỏi bằng Gemini AI.
     *
     * <p>Có 2 mode:
     * <ul>
     *   <li>{@code saveToDb=false} – Preview: Gemini sinh câu hỏi, trả về nhưng không lưu DB.</li>
     *   <li>{@code saveToDb=true}  – Sinh và lưu ngay vào DB.</li>
     * </ul>
     *
     * <p>Request body ví dụ:
     * <pre>
     * {
     *   "bankId": 5,
     *   "subject": "Chemistry",
     *   "topic": "Periodic Table",
     *   "difficulty": "MEDIUM",
     *   "type": "MULTIPLE_CHOICE",
     *   "count": 5,
     *   "saveToDb": false
     * }
     * </pre>
     */
    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(
            summary = "Sinh câu hỏi bằng AI (Gemini)",
            description = "Sinh câu hỏi từ Gemini AI. saveToDb=false để preview, saveToDb=true để lưu ngay."
    )
    public ResponseEntity<List<QuestionDTO>> aiGenerateQuestions(
            @Valid @RequestBody AIGenerateQuestionsRequest request,
            @AuthenticationPrincipal User user) {

        List<QuestionDTO> result = questionService.aiGenerateQuestions(request, user);
        // Nếu đã lưu DB → 201 Created; nếu chỉ preview → 200 OK
        return request.isSaveToDb()
                ? ResponseEntity.status(201).body(result)
                : ResponseEntity.ok(result);
    }

    /**
     * [SAVE BATCH] Lưu danh sách câu hỏi đã được preview và chỉnh sửa vào DB.
     *
     * <p>FE gọi endpoint này sau khi user review + chỉnh sửa câu hỏi AI sinh ra.
     *
     * <p>Request body ví dụ:
     * <pre>
     * {
     *   "bankId": 5,
     *   "questions": [ { "content": "...", "type": "MULTIPLE_CHOICE", ... } ]
     * }
     * </pre>
     */
    /**
     * [SAVE BATCH] Lưu danh sách câu hỏi đã được preview và chỉnh sửa vào DB.
     *
     * <p>FE gọi endpoint này sau khi user review + chỉnh sửa câu hỏi AI sinh ra.
     *
     * <p>Request body ví dụ:
     * <pre>
     * {
     *   "bankId": 5,
     *   "questions": [ { "content": "...", "type": "MULTIPLE_CHOICE", ... } ]
     * }
     * </pre>
     */
    @PostMapping("/save-batch")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(
            summary = "Lưu danh sách câu hỏi đã preview vào DB",
            description = "Dùng sau khi user chỉnh sửa câu hỏi AI sinh ra, gọi để lưu vào ngân hàng."
    )
    public ResponseEntity<List<QuestionDTO>> savePreviewedQuestions(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        Integer bankId = (Integer) body.get("bankId");
        @SuppressWarnings("unchecked")
        List<QuestionDTO> questions = (List<QuestionDTO>) body.get("questions");

        // FE gửi raw maps, cần deserialize thủ công
        return ResponseEntity.status(201)
                .body(questionService.savePreviewedQuestions(bankId, questions, user));
    }

    /**
     * [APPROVE] Duyệt hoặc huỷ duyệt một câu hỏi.
     *
     * <p>Chỉ Manager mới có quyền thực hiện.
     *
     * <p>Request body:
     * <pre>
     * { "approve": true }   // Duyệt
     * { "approve": false }  // Huỷ duyệt
     * </pre>
     *
     * <p>Response trả về QuestionDTO đã cập nhật (kèm approvedById, approvedByName).
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Duyệt / huỷ duyệt câu hỏi",
            description = "Chỉ Manager mới có quyền. Body: {\"approve\": true/false}"
    )
    public ResponseEntity<QuestionDTO> approveQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal User manager) {

        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        return ResponseEntity.ok(questionService.approveQuestion(id, approve, manager));
    }
}

