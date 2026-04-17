package com.planbookai.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * UserAnalyticsDTO – Response của API GET /api/v1/analytics/users (Admin only).
 *
 * <p>Cung cấp báo cáo toàn diện về người dùng hệ thống:
 * <ul>
 *   <li>{@code summary}           – KPI tổng quan: tổng users, active, new this month</li>
 *   <li>{@code usersByRole}       – Phân bổ user theo role</li>
 *   <li>{@code usersByMonth}      – Đăng ký mới theo từng tháng năm hiện tại</li>
 *   <li>{@code activeVsInactive}  – Tỉ lệ tài khoản active / inactive</li>
 *   <li>{@code topTeachers}       – Top 5 teacher theo số đề thi đã tạo</li>
 *   <li>{@code subscriptionStats} – Thống kê subscription hiện tại của users</li>
 *   <li>{@code recentUsers}       – 10 user mới nhất</li>
 * </ul>
 */
public record UserAnalyticsDTO(

        LocalDateTime generatedAt,

        /** Tổng quan KPI người dùng. */
        UserSummaryDTO summary,

        /**
         * Số user theo role.
         * Key: "ADMIN" | "MANAGER" | "STAFF" | "TEACHER", Value: số lượng.
         */
        Map<String, Long> usersByRole,

        /**
         * Số user đăng ký mới theo từng tháng trong năm hiện tại.
         * Key: "yyyy-MM" (e.g. "2026-01"), Value: số user mới.
         */
        Map<String, Long> usersByMonth,

        /** Tỉ lệ active vs inactive. */
        ActiveStatusDTO activeVsInactive,

        /** Top 5 teacher theo số đề thi đã tạo. */
        List<TeacherActivityDTO> topTeachers,

        /** Thống kê subscription hiện tại của users (có gói, không có gói). */
        SubscriptionStatsDTO subscriptionStats,

        /** 10 user mới đăng ký gần nhất. */
        List<RecentUserDTO> recentUsers
) {

    // ── Nested records ────────────────────────────────────────────────────────

    /** KPI tổng quan người dùng. */
    public record UserSummaryDTO(
            long totalUsers,
            long activeUsers,
            long inactiveUsers,
            /** Số user đăng ký tháng này. */
            long newUsersThisMonth,
            /** Số user đăng ký tháng trước. */
            long newUsersLastMonth,
            /** Tăng trưởng user MoM (%). Null nếu tháng trước = 0. */
            Double userGrowthRate,
            long totalTeachers,
            long totalManagers,
            long totalAdmins,
            long totalStaff,
            /** Số user có ít nhất 1 order ACTIVE (đang dùng gói trả phí). */
            long subscribedUsers
    ) {}

    /** Tỉ lệ tài khoản active / inactive. */
    public record ActiveStatusDTO(
            long   activeCount,
            long   inactiveCount,
            double activeRatio,
            double inactiveRatio
    ) {}

    /** Hoạt động của một teacher (số đề thi, câu hỏi). */
    public record TeacherActivityDTO(
            Long   teacherId,
            String teacherName,
            String teacherEmail,
            long   examCount,
            long   publishedExamCount,
            long   questionBankCount,
            LocalDateTime lastLogin   // createdAt của exam cuối cùng (proxy)
    ) {}

    /** Thống kê subscription của users. */
    public record SubscriptionStatsDTO(
            /** Số user đang có gói ACTIVE. */
            long subscribedUsers,
            /** Số user chưa có gói hoặc đã expired. */
            long unsubscribedUsers,
            double subscriptionRate,
            /** Phân bổ theo tên gói: { "Gói Premium": 60, "Gói Basic": 35 } */
            Map<String, Long> byPackage
    ) {}

    /** Thông tin tóm tắt một user mới. */
    public record RecentUserDTO(
            Long          userId,
            String        fullName,
            String        email,
            String        role,
            Boolean       isActive,
            Boolean       emailVerified,
            /** Tên gói subscription đang dùng (null nếu chưa đăng ký). */
            String        activePackage,
            LocalDateTime createdAt
    ) {}
}
