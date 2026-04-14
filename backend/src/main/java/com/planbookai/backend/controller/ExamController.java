package com.planbookai.backend.controller;

import com.planbookai.backend.dto.AIGenerateExamRequest;
import com.planbookai.backend.dto.ExamDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * ExamController – REST API quản lý đề thi và sinh đề bằng AI.
 *
 * <p>Base URL: /api/v1/exams
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET    /exams                – Danh sách đề của teacher (phân trang, lọc)</li>
 *   <li>GET    /exams/{id}           – Chi tiết đề + câu hỏi</li>
 *   <li>POST   /exams/ai-generate    – Sinh đề từ bank + AI (KAN-23)</li>
 *   <li>PUT    /exams/{id}/publish   – Publish đề</li>
 *   <li>DELETE /exams/{id}           – Xóa đề</li>
 * </ul>
 *
 * <h2>Luồng AI Generate (KAN-23):</h2>
 * <ol>
 *   <li>Teacher gọi POST /exams/ai-generate với tham số môn, topic, số câu, bankIds.</li>
 *   <li>ExamService lấy câu từ bank → tính gap → gọi AI sinh bù.</li>
 *   <li>Trả về ExamDTO đầy đủ, đề ở trạng thái DRAFT.</li>
 *   <li>Teacher review → gọi PUT /exams/{id}/publish để phát hành.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/exams")
@Tag(name = "Exams", description = "Quản lý đề thi và sinh đề bằng AI kết hợp ngân hàng câu hỏi")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    // =========================================================================
    // GET /exams
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Danh sách đề thi của teacher",
            description = """
                    Trả về danh sách đề thi của teacher đang đăng nhập (phân trang).
                    Hỗ trợ lọc theo môn học và trạng thái (DRAFT, PUBLISHED, CLOSED).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
    })
    public ResponseEntity<PageResponse<ExamDTO>> getMyExams(
            @AuthenticationPrincipal User teacher,
            @Parameter(description = "Trang (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Lọc theo môn học", example = "Chemistry")
            @RequestParam(required = false) String subject,
            @Parameter(description = "Lọc theo trạng thái: DRAFT | PUBLISHED | CLOSED")
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(examService.getMyExams(teacher, page, size, subject, status));
    }

    // =========================================================================
    // GET /exams/{id}
    // =========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Chi tiết đề thi",
            description = "Trả về đề thi đầy đủ kèm danh sách câu hỏi (source: BANK | AI)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy đề thi"),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề thi", content = @Content),
    })
    public ResponseEntity<ExamDTO> getExamDetail(
            @Parameter(description = "ID đề thi", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(examService.getExamDetail(id, user));
    }

    // =========================================================================
    // POST /exams/ai-generate   (KAN-23)
    // =========================================================================

    /**
     * [KAN-23] Sinh đề thi tự động bằng AI kết hợp ngân hàng câu hỏi.
     *
     * <p>Hệ thống sẽ:
     * <ol>
     *   <li>Lấy câu hỏi đã duyệt từ các bank được chỉ định.</li>
     *   <li>Nếu không đủ số câu cần thiết, gọi Gemini AI để sinh phần còn thiếu.</li>
     *   <li>Lưu đề thi vào DB ở trạng thái DRAFT.</li>
     * </ol>
     */
    @PostMapping("/ai-generate")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Sinh đề thi bằng AI + ngân hàng câu hỏi (KAN-23)",
            description = """
                    Sinh đề thi tự động theo chiến lược:
                    1. **Lấy từ bank:** Truy vấn các bank được chỉ định (bankIds), lọc theo môn, topic, độ khó, loại câu.
                    2. **Tính gap:** `gap = totalQuestions - câu từ bank`
                    3. **AI fill gap:** Nếu gap > 0, gọi Gemini AI sinh thêm câu, truyền nội dung câu đã có để tránh trùng.
                    4. **Lưu đề:** Tạo Exam ở trạng thái DRAFT, gắn tất cả câu hỏi (BANK + AI).

                    **Response:** ExamDTO đầy đủ với `source` = `BANK` hoặc `AI` cho từng câu.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo đề thành công (DRAFT)"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền (yêu cầu TEACHER)", content = @Content),
    })
    public ResponseEntity<ExamDTO> aiGenerateExam(
            @Valid @RequestBody AIGenerateExamRequest request,
            @AuthenticationPrincipal User teacher) {
        ExamDTO result = examService.aiGenerateExam(request, teacher);
        return ResponseEntity.status(201).body(result);
    }

    // =========================================================================
    // PUT /exams/{id}/publish
    // =========================================================================

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "Publish đề thi",
            description = "Chuyển đề từ trạng thái DRAFT → PUBLISHED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publish thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đề thi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề thi", content = @Content),
    })
    public ResponseEntity<ExamDTO> publishExam(
            @Parameter(description = "ID đề thi", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User teacher) {
        return ResponseEntity.ok(examService.publishExam(id, teacher));
    }

    // =========================================================================
    // DELETE /exams/{id}
    // =========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @Operation(
            summary = "Xóa đề thi",
            description = "Xóa vĩnh viễn đề thi và tất cả liên kết câu hỏi (exam_questions)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không phải chủ đề thi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề thi", content = @Content),
    })
    public ResponseEntity<Void> deleteExam(
            @Parameter(description = "ID đề thi", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User teacher) {
        examService.deleteExam(id, teacher);
        return ResponseEntity.noContent().build();
    }
}
