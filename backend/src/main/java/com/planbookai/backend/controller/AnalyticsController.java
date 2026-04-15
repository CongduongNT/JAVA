package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ExamAnalyticsDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AnalyticsController – REST API thống kê và báo cáo (KAN-26).
 *
 * <p>Base URL: /api/v1/analytics
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /analytics/exams/{id}/results – Thống kê kết quả một đề thi</li>
 * </ul>
 *
 * <h2>Luồng (KAN-26):</h2>
 * <ol>
 *   <li>Teacher / Manager / Admin gọi GET /analytics/exams/{id}/results.</li>
 *   <li>AnalyticsService kiểm tra quyền, load câu hỏi, tính toán số liệu.</li>
 *   <li>Trả về {@link ExamAnalyticsDTO} gồm avg_score, pass_rate,
 *       score_distribution, question_stats, difficulty_stats, ai_vs_bank.</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Thống kê và báo cáo kết quả đề thi (KAN-26)")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // =========================================================================
    // GET /analytics/exams/{id}/results   (KAN-26)
    // =========================================================================

    /**
     * [KAN-26] Thống kê kết quả đề thi.
     *
     * <p>Trả về:
     * <pre>
     * {
     *   "examId":            1,
     *   "examTitle":         "Đề kiểm tra Hóa Lớp 10 – Nguyên tử Phân tử",
     *   "subject":           "Hóa học",
     *   "gradeLevel":        "10",
     *   "topic":             "Nguyên tử – Phân tử",
     *   "totalQuestions":    20,
     *   "durationMins":      45,
     *   "status":            "PUBLISHED",
     *   "avgScore":          6.85,
     *   "passRate":          73.5,
     *   "scoreDistribution": { "0-2": 5, "3-4": 10, "5-6": 35, "7-8": 35, "9-10": 15 },
     *   "questionStats":     [ { "questionId": 1, "orderIndex": 0, "content": "...",
     *                            "difficulty": "MEDIUM", "type": "MULTIPLE_CHOICE",
     *                            "source": "BANK", "points": 1.0,
     *                            "estimatedCorrectRate": 60.0 }, ... ],
     *   "difficultyStats":   { "EASY": 5, "MEDIUM": 10, "HARD": 5 },
     *   "aiVsBankStats":     { "bankCount": 12, "aiCount": 8,
     *                          "bankRatio": 60.0, "aiRatio": 40.0 }
     * }
     * </pre>
     */
    @GetMapping("/exams/{id}/results")
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Thống kê kết quả đề thi (KAN-26)",
            description = """
                    **[KAN-26] GET /api/v1/analytics/exams/{id}/results**

                    Trả về thống kê toàn diện cho một đề thi:

                    | Field                | Mô tả                                              |
                    |----------------------|----------------------------------------------------|
                    | `avg_score`          | Điểm trung bình ước tính (thang 10)                |
                    | `pass_rate`          | Tỉ lệ đạt (%): điểm ≥ 5                           |
                    | `score_distribution` | Phân phối điểm: 0-2, 3-4, 5-6, 7-8, 9-10          |
                    | `question_stats`     | Thống kê từng câu: độ khó, tỉ lệ đúng ước lượng   |
                    | `difficulty_stats`   | Số câu EASY / MEDIUM / HARD                        |
                    | `ai_vs_bank_stats`   | Tỉ lệ câu từ AI vs ngân hàng                       |

                    ### Quyền truy cập
                    - **TEACHER**: chỉ xem đề của mình
                    - **MANAGER / ADMIN**: xem tất cả đề

                    ### Ghi chú
                    Trong phiên bản hiện tại, `avg_score`, `pass_rate` và `score_distribution`
                    được **ước lượng** theo chuẩn Bloom's Taxonomy từ cấu trúc độ khó của đề.
                    Khi tích hợp `exam_submissions`, các trường này sẽ được tính từ dữ liệu thực.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description  = "Thống kê trả về thành công",
                    content      = @Content(schema = @Schema(implementation = ExamAnalyticsDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực",              content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền xem đề này",  content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề thi",       content = @Content),
    })
    public ResponseEntity<ExamAnalyticsDTO> getExamResults(
            @Parameter(description = "ID đề thi", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ExamAnalyticsDTO analytics = analyticsService.getExamAnalytics(id, user);
        return ResponseEntity.ok(analytics);
    }
}
