package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AIGenerateQuestionsRequest;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * QuestionController – REST API quản lý câu hỏi (CRUD + AI Generate + Approval).
 *
 * <p>Base URL: /api/v1/questions
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /questions               – Lấy danh sách câu hỏi (lọc theo trạng thái duyệt, Manager/Admin)</li>
 *   <li>GET    /questions/{id}          – Xem chi tiết 1 câu hỏi</li>
 *   <li>DELETE /questions/{id}          – Xóa câu hỏi</li>
 *   <li>POST   /questions/ai-generate   – Sinh câu hỏi bằng AI (preview hoặc lưu)</li>
 *   <li>POST   /questions/save-batch    – Lưu danh sách câu hỏi đã preview/chỉnh sửa</li>
 *   <li>PUT    /questions/{id}/approve  – Duyệt / huỷ duyệt câu hỏi (Manager only)</li>
 * </ul>
 *
 * <h2>Luồng AI Generate (2 bước):</h2>
 * <ol>
 *   <li>FE gọi POST /questions/ai-generate với {@code saveToDb=false} → nhận preview.</li>
 *   <li>Sau khi user review/chỉnh sửa, FE gọi POST /questions/save-batch → lưu vào DB.</li>
 * </ol>
 *
 * <h2>Luồng Approval:</h2>
 * <ol>
 *   <li>Manager gọi GET /questions?approved=false để lấy danh sách câu hỏi chờ duyệt.</li>
 *   <li>Manager gọi PUT /questions/{id}/approve với body {@code {"approve": true}} để duyệt.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Questions", description = "Quản lý câu hỏi, sinh câu hỏi bằng AI, và phê duyệt nội dung (Manager)")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // =========================================================================
    // GET /questions   – Lấy danh sách câu hỏi (lọc approval)
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @Operation(
            summary = "Lấy danh sách câu hỏi (có lọc theo trạng thái duyệt)",
            description = """
                    Trả về danh sách câu hỏi trong toàn hệ thống. Hỗ trợ lọc theo trạng thái phê duyệt:
                    - `approved=false` → Câu hỏi **chờ duyệt** (pending)
                    - `approved=true`  → Câu hỏi **đã duyệt**
                    - _(không truyền)_ → **Tất cả** câu hỏi

                    **Yêu cầu role:** MANAGER hoặc ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực (thiếu JWT)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền (yêu cầu MANAGER/ADMIN)", content = @Content),
    })
    public ResponseEntity<List<QuestionDTO>> getQuestions(
            @Parameter(
                    name = "approved",
                    description = "Lọc theo trạng thái duyệt: `true` = đã duyệt, `false` = chờ duyệt, bỏ qua = tất cả",
                    example = "false",
                    required = false,
                    schema = @Schema(type = "boolean")
            )
            @RequestParam(required = false) Boolean approved) {
        return ResponseEntity.ok(questionService.getQuestionsByApprovalStatus(approved));
    }

    // =========================================================================
    // GET /questions/{id}   – Xem chi tiết câu hỏi
    // =========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Xem chi tiết câu hỏi",
            description = "Trả về thông tin đầy đủ của 1 câu hỏi, bao gồm options, đáp án, trạng thái duyệt và người duyệt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy câu hỏi"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy câu hỏi với ID đã cho", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
    })
    public ResponseEntity<QuestionDTO> getQuestion(
            @Parameter(description = "ID của câu hỏi", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    // =========================================================================
    // DELETE /questions/{id}   – Xóa câu hỏi
    // =========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(summary = "Xóa câu hỏi", description = "Xóa vĩnh viễn câu hỏi khỏi ngân hàng.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy câu hỏi", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền", content = @Content),
    })
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "ID của câu hỏi cần xóa", example = "1", required = true)
            @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // POST /questions/ai-generate   – Sinh AI
    // =========================================================================

    /**
     * [AI GENERATE] Sinh câu hỏi bằng Gemini AI.
     *
     * <p>Có 2 mode:
     * <ul>
     *   <li>{@code saveToDb=false} – Preview: Gemini sinh câu hỏi, trả về nhưng không lưu DB.</li>
     *   <li>{@code saveToDb=true}  – Sinh và lưu ngay vào DB.</li>
     * </ul>
     */
    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(
            summary = "Sinh câu hỏi bằng AI (Gemini)",
            description = """
                    Sinh câu hỏi từ Gemini AI dựa trên môn học, chủ đề và độ khó.

                    **Hai chế độ:**
                    - `saveToDb=false` – Preview: trả về câu hỏi nhưng **không lưu** vào DB.
                    - `saveToDb=true`  – Sinh và **lưu ngay** vào DB.

                    Khuyến nghị luồng 2 bước: preview trước → review → lưu batch qua `/save-batch`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preview thành công (saveToDb=false)"),
            @ApiResponse(responseCode = "201", description = "Sinh và lưu thành công (saveToDb=true)"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền", content = @Content),
    })
    public ResponseEntity<List<QuestionDTO>> aiGenerateQuestions(
            @Valid @RequestBody AIGenerateQuestionsRequest request,
            @AuthenticationPrincipal User user) {

        List<QuestionDTO> result = questionService.aiGenerateQuestions(request, user);
        return request.isSaveToDb()
                ? ResponseEntity.status(201).body(result)
                : ResponseEntity.ok(result);
    }

    // =========================================================================
    // POST /questions/save-batch   – Lưu batch
    // =========================================================================

    /**
     * [SAVE BATCH] Lưu danh sách câu hỏi đã được preview và chỉnh sửa vào DB.
     *
     * <p>FE gọi endpoint này sau khi user review + chỉnh sửa câu hỏi AI sinh ra.
     */
    @PostMapping("/save-batch")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    @Operation(
            summary = "Lưu danh sách câu hỏi đã preview vào DB",
            description = """
                    Lưu hàng loạt câu hỏi đã được review/chỉnh sửa sau bước preview AI.

                    **Request body:**
                    ```json
                    {
                      "bankId": 5,
                      "questions": [
                        { "content": "...", "type": "MULTIPLE_CHOICE", "difficulty": "MEDIUM", "options": [...] }
                      ]
                    }
                    ```
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lưu thành công, trả về danh sách câu hỏi đã có ID"),
            @ApiResponse(responseCode = "400", description = "bankId không hợp lệ hoặc danh sách rỗng", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền", content = @Content),
    })
    public ResponseEntity<List<QuestionDTO>> savePreviewedQuestions(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User user) {

        Integer bankId = (Integer) body.get("bankId");
        @SuppressWarnings("unchecked")
        List<QuestionDTO> questions = (List<QuestionDTO>) body.get("questions");

        return ResponseEntity.status(201)
                .body(questionService.savePreviewedQuestions(bankId, questions, user));
    }

    // =========================================================================
    // PUT /questions/{id}/approve   – Duyệt / huỷ duyệt
    // =========================================================================

    /**
     * [APPROVE] Duyệt hoặc huỷ duyệt một câu hỏi. Chỉ Manager mới có quyền.
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(
            summary = "Duyệt / huỷ duyệt câu hỏi (Manager only)",
            description = """
                    Duyệt hoặc huỷ duyệt một câu hỏi trong ngân hàng.

                    **Chỉ Manager mới có quyền gọi API này.**

                    **Request body:**
                    ```json
                    { "approve": true }   // Duyệt câu hỏi
                    { "approve": false }  // Huỷ duyệt câu hỏi
                    ```

                    **Response:** QuestionDTO đã cập nhật, kèm `approvedById` và `approvedByName`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái duyệt thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy câu hỏi với ID đã cho", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền (yêu cầu MANAGER)", content = @Content),
    })
    public ResponseEntity<QuestionDTO> approveQuestion(
            @Parameter(description = "ID của câu hỏi cần duyệt", example = "1", required = true)
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Trạng thái duyệt: `approve=true` để duyệt, `approve=false` để huỷ duyệt",
                    required = true
            )
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal User manager) {

        boolean approve = Boolean.TRUE.equals(body.get("approve"));
        return ResponseEntity.ok(questionService.approveQuestion(id, approve, manager));
    }
}
