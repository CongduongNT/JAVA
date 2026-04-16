package com.planbookai.backend.controller;

import com.planbookai.backend.dto.*;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.GradingService;
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
 * GradingController – REST API cho module chấm điểm bài thi.
 *
 * <p>Base URL: /api/v1/grading-results
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /grading-results?exam_id={id} – Danh sách kết quả chấm (phân trang)</li>
 *   <li>GET  /grading-results/{id}          – Chi tiết 1 kết quả (gồm từng câu)</li>
 *   <li>PUT  /grading-results/{id}/feedback – Cập nhật feedback của giáo viên</li>
 * </ul>
 *
 * <p>Lưu ý: OCR System gọi trực tiếp GradingService (không qua REST) để chấm điểm.
 * Controller này chỉ phục vụ teacher xem kết quả và nhập feedback.
 */
@RestController
@RequestMapping("/api/v1/grading-results")
@Tag(name = "Grading Results", description = "Xem kết quả chấm bài thi và nhập feedback")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    // =========================================================================
    // GET /grading-results?exam_id={id}
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Danh sách kết quả chấm theo bài thi",
            description = """
                    Trả về danh sách kết quả chấm của tất cả học sinh cho 1 bài thi.
                    Phân trang, sắp xếp theo thời điểm chấm (mới nhất trước).
                    Chỉ giáo viên sở hữu bài thi mới được xem.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập bài thi", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài thi", content = @Content),
    })
    public ResponseEntity<PageResponse<GradingResultSummaryDTO>> getGradingResults(
            @AuthenticationPrincipal User teacher,
            @Parameter(description = "ID bài thi", example = "1", required = true)
            @RequestParam Long examId,
            @Parameter(description = "Trang (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                gradingService.getGradingResultsByExam(examId, teacher.getId(), page, size));
    }

    // =========================================================================
    // GET /grading-results/{id}
    // =========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Chi tiết 1 kết quả chấm",
            description = """
                    Trả về chi tiết đầy đủ của 1 kết quả chấm, gồm:
                    <ul>
                      <li>Thông tin tổng quát (điểm, %, số câu đúng/sai/trống)</li>
                      <li>Danh sách chi tiết từng câu hỏi (đáp án OCR, đáp án đúng, đúng/sai)</li>
                      <li>Feedback của giáo viên (nếu có)</li>
                    </ul>
                    Chỉ giáo viên sở hữu bài thi mới được xem.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy kết quả"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kết quả", content = @Content),
    })
    public ResponseEntity<GradingResultDetailDTO> getGradingResultDetail(
            @Parameter(description = "ID kết quả chấm", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User teacher) {

        return ResponseEntity.ok(
                gradingService.getGradingResultDetail(id, teacher.getId()));
    }

    // =========================================================================
    // PUT /grading-results/{id}/feedback
    // =========================================================================

    @PutMapping("/{id}/feedback")
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Cập nhật feedback cho kết quả chấm",
            description = """
                    Cập nhật nhận xét của giáo viên cho 1 kết quả chấm.

                    ### Body
                    ```json
                    {
                      "teacherFeedback": "Em làm tốt phần trắc nghiệm...",
                      "requestAiFeedback": true
                    }
                    ```

                    ### Luồng AI feedback
                    <ol>
                      <li>Teacher gửi `requestAiFeedback: true`</li>
                      <li>BE gọi Gemini sinh gợi ý dựa trên dữ liệu bài làm</li>
                      <li>AI suggestion được trả về trong `aiFeedbackSuggestion`</li>
                      <li>FE hiển thị để teacher copy/edit trước khi lưu bản cuối</li>
                      <li>Teacher gửi lại với `teacherFeedback` đã chỉnh sửa</li>
                    </ol>

                    ### Ghi chú
                    <ul>
                      <li>AI feedback KHÔNG tự động lưu – teacher phải POST lại</li>
                      <li>`teacherFeedback: null` → giữ nguyên feedback cũ</li>
                      <li>`feedbackSource` = MANUAL | AI_EDITED</li>
                    </ul>
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Exam chưa được grade hoặc dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy kết quả", content = @Content),
    })
    public ResponseEntity<UpdateFeedbackResponse> updateFeedback(
            @Parameter(description = "ID kết quả chấm", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateFeedbackRequest request,
            @AuthenticationPrincipal User teacher) {

        return ResponseEntity.ok(
                gradingService.updateFeedback(id, request, teacher.getId()));
    }
}
