package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ExamAnalyticsDTO;
import com.planbookai.backend.dto.RevenueAnalyticsDTO;
import com.planbookai.backend.dto.StudentAnalyticsDTO;
import com.planbookai.backend.dto.UserAnalyticsDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AnalyticsService;
import com.planbookai.backend.service.RevenueService;
import com.planbookai.backend.service.UserAnalyticsService;
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
 *   <li>GET /analytics/students           – Teacher xem tổng quan nhóm học sinh mục tiêu</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Thống kê và báo cáo kết quả đề thi (KAN-26)")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService     analyticsService;
    private final RevenueService       revenueService;
    private final UserAnalyticsService userAnalyticsService;

    // =========================================================================
    // GET /analytics/exams/{id}/results   (KAN-26)
    // =========================================================================

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

                    **Quyền:** TEACHER xem đề của mình, MANAGER/ADMIN xem tất cả.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thống kê trả về thành công",
                    content = @Content(schema = @Schema(implementation = ExamAnalyticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực",             content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền xem đề này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đề thi",      content = @Content),
    })
    public ResponseEntity<ExamAnalyticsDTO> getExamResults(
            @Parameter(description = "ID đề thi", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(analyticsService.getExamAnalytics(id, user));
    }

    // =========================================================================
    // GET /analytics/students   (KAN-26)
    // =========================================================================

    /**
     * [KAN-26] Tổng quan nhóm học sinh mục tiêu của teacher đang đăng nhập.
     *
     * <p>Vì hệ thống chưa có entity Student,  analytics được tổng hợp từ
     * tất cả đề thi của teacher, <strong>phân nhóm theo (gradeLevel × subject)</strong>.
     * Mỗi nhóm ứng với một lớp/môn mà teacher đang phụ trách.
     */
    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('TEACHER','MANAGER','ADMIN')")
    @Operation(
            summary = "Tổng quan học sinh mục tiêu của teacher (KAN-26)",
            description = """
                    **[KAN-26] GET /api/v1/analytics/students**

                    Trả về analytics nhóm học sinh mục tiêu của teacher đang đăng nhập,
                    phân tích từ cấu trúc các đề thi đã tạo (grade_level × subject).

                    ### Response structure

                    | Field                  | Mô tả                                           |
                    |------------------------|-------------------------------------------------|
                    | `summary`              | Tổng số đề, câu hỏi, bank, nhóm HS             |
                    | `studentGroups`        | Danh sách nhóm HS (khối × môn), có metrics      |
                    | `examsBySubject`       | Số đề theo môn học                              |
                    | `examsByGrade`         | Số đề theo khối lớp                             |
                    | `questionsByDifficulty`| Số câu EASY / MEDIUM / HARD toàn bộ            |
                    | `topTopics`            | Top 5 chủ đề xuất hiện nhiều nhất               |

                    ### studentGroups – mỗi nhóm bao gồm
                    - `gradeLevel`, `subject` – định danh nhóm
                    - `examCount`, `publishedExamCount`, `totalQuestions`
                    - `estimatedAvgScore`, `estimatedPassRate` – ước lượng Bloom's Taxonomy
                    - `difficultyBreakdown` – phân bổ EASY / MEDIUM / HARD
                    - `aiQuestionRatio` – % câu do AI tạo
                    - `topics` – danh sách chủ đề đã ra
                    - `recentExams` – 5 đề gần nhất của nhóm

                    ### Ghi chú
                    `estimatedAvgScore` và `estimatedPassRate` được tính theo chuẩn
                    Bloom's Taxonomy (EASY=8.5, MEDIUM=6.5, HARD=4.5 / thang 10).
                    Khi bổ sung `exam_submissions`, sẽ thay bằng dữ liệu thực tế.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trả về analytics thành công",
                    content = @Content(schema = @Schema(implementation = StudentAnalyticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực",  content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền", content = @Content),
    })
    public ResponseEntity<StudentAnalyticsDTO> getStudentAnalytics(
            @AuthenticationPrincipal User teacher) {

        return ResponseEntity.ok(analyticsService.getStudentAnalytics(teacher));
    }

    // =========================================================================
    // GET /analytics/revenue   (KAN-26)
    // =========================================================================

    /**
     * [KAN-26] Báo cáo doanh thu toàn hệ thống – chỉ Admin và Manager.
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @Operation(
            summary = "Báo cáo doanh thu hệ thống (KAN-26)",
            description = """
                    **[KAN-26] GET /api/v1/analytics/revenue**

                    Báo cáo doanh thu toàn hệ thống, chỉ dành cho **ADMIN** và **MANAGER**.

                    ### Response structure

                    | Field                  | Mô tả                                                    |
                    |------------------------|----------------------------------------------------------|
                    | `summary`              | KPIs: tổng revenue, tháng này, MoM growth, unique buyers |
                    | `revenueByMonth`       | Doanh thu 12 tháng năm hiện tại (key: "yyyy-MM")         |
                    | `revenueByPackage`     | Doanh thu theo từng gói subscription                     |
                    | `ordersByStatus`       | Số order: PENDING / ACTIVE / EXPIRED / CANCELLED         |
                    | `topPackages`          | Top 5 gói bán chạy nhất (kèm revenue share %)            |
                    | `paymentMethodStats`   | Số đơn theo phương thức thanh toán                       |
                    | `recentOrders`         | 10 đơn hàng gần nhất (mọi trạng thái)                   |

                    ### Nguồn dữ liệu
                    - Doanh thu chỉ tính từ orders có `status = ACTIVE`.
                    - MoM Growth = ((tháng này - tháng trước) / tháng trước) × 100%.
                    - `avgOrderValue` = tổng revenue / số ACTIVE orders.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Báo cáo trả về thành công",
                    content = @Content(schema = @Schema(implementation = RevenueAnalyticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực",                         content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền (yêu cầu ADMIN/MANAGER)", content = @Content),
    })
    public ResponseEntity<RevenueAnalyticsDTO> getRevenueAnalytics() {
        return ResponseEntity.ok(revenueService.getRevenueAnalytics());
    }

    // =========================================================================
    // GET /analytics/users   (KAN-26)
    // =========================================================================

    /**
     * [KAN-26] Báo cáo tổng quan người dùng hệ thống – chỉ Admin.
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Báo cáo người dùng hệ thống (KAN-26)",
            description = """
                    **[KAN-26] GET /api/v1/analytics/users**

                    Báo cáo toàn diện về người dùng, chỉ dành cho **ADMIN**.

                    ### Response structure

                    | Field                | Mô tả                                                 |
                    |----------------------|-------------------------------------------------------|
                    | `summary`            | KPIs: tổng, active, new this month, MoM growth        |
                    | `usersByRole`        | Số user theo role (ADMIN/MANAGER/STAFF/TEACHER)       |
                    | `usersByMonth`       | Đăng ký mới theo tháng năm hiện tại               |
                    | `activeVsInactive`   | Tỉ lệ account active / inactive                      |
                    | `topTeachers`        | Top 5 teacher theo số đề thi đã tạo                 |
                    | `subscriptionStats`  | Số user có gói / chưa gói, phân bổ theo gói        |
                    | `recentUsers`        | 10 user mới đăng ký gần nhất                       |

                    ### Quyền truy cập
                    Chỉ **ADMIN** – trả `403` với MANAGER, TEACHER.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Báo cáo trả về thành công",
                    content = @Content(schema = @Schema(implementation = UserAnalyticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực",                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền (yêu cầu ADMIN)",   content = @Content),
    })
    public ResponseEntity<UserAnalyticsDTO> getUserAnalytics() {
        return ResponseEntity.ok(userAnalyticsService.getUserAnalytics());
    }
}
