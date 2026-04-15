package com.planbookai.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RevenueAnalyticsDTO – Response của API GET /api/v1/analytics/revenue.
 *
 * <p>Cung cấp báo cáo doanh thu toàn hệ thống dành cho ADMIN và MANAGER:
 * <ul>
 *   <li>{@code summary}           – Tổng quan doanh thu (tổng, tháng này, tăng trưởng)</li>
 *   <li>{@code revenueByMonth}    – Doanh thu theo từng tháng trong năm hiện tại</li>
 *   <li>{@code revenueByPackage}  – Doanh thu phân theo gói subscription</li>
 *   <li>{@code ordersByStatus}    – Số order theo trạng thái</li>
 *   <li>{@code recentOrders}      – 10 order gần nhất</li>
 *   <li>{@code topPackages}       – Top 5 gói được mua nhiều nhất</li>
 *   <li>{@code paymentMethodStats}– Phân bổ phương thức thanh toán</li>
 * </ul>
 */
public record RevenueAnalyticsDTO(

        /** Thời điểm tính toán analytics. */
        LocalDateTime generatedAt,

        /** Tổng quan doanh thu. */
        RevenueSummaryDTO summary,

        /**
         * Doanh thu theo tháng trong năm hiện tại.
         * Key: "YYYY-MM" (e.g. "2026-01"), Value: tổng doanh thu tháng đó.
         */
        Map<String, BigDecimal> revenueByMonth,

        /**
         * Doanh thu theo tên gói subscription.
         * Key: tên gói, Value: tổng doanh thu.
         */
        Map<String, BigDecimal> revenueByPackage,

        /**
         * Số đơn hàng theo trạng thái.
         * Key: "PENDING" | "ACTIVE" | "EXPIRED" | "CANCELLED", Value: số lượng.
         */
        Map<String, Long> ordersByStatus,

        /** Top 5 gói subscription bán chạy nhất. */
        List<PackageRevenueDTO> topPackages,

        /**
         * Phân bổ phương thức thanh toán.
         * Key: tên method (VNPay, Momo, …), Value: số lượng order.
         */
        Map<String, Long> paymentMethodStats,

        /** 10 đơn hàng gần nhất (ACTIVE). */
        List<RecentOrderDTO> recentOrders
) {

    // ── Nested records ────────────────────────────────────────────────────────

    /** Tổng quan KPI doanh thu. */
    public record RevenueSummaryDTO(
            /** Tổng doanh thu toàn thời gian (chỉ tính ACTIVE orders). */
            BigDecimal totalRevenue,

            /** Tổng doanh thu tháng hiện tại. */
            BigDecimal currentMonthRevenue,

            /** Tổng doanh thu tháng trước. */
            BigDecimal lastMonthRevenue,

            /** Tăng trưởng MoM (%): ((curr - last) / last) * 100. Null nếu tháng trước = 0. */
            Double monthOverMonthGrowth,

            /** Tổng doanh thu năm hiện tại. */
            BigDecimal currentYearRevenue,

            /** Tổng số đơn hàng (mọi trạng thái). */
            long totalOrders,

            /** Số đơn ACTIVE. */
            long activeOrders,

            /** Số đơn đang PENDING. */
            long pendingOrders,

            /** Số người dùng đã mua ít nhất 1 gói (unique buyers). */
            long uniqueBuyers,

            /** Giá trị trung bình mỗi đơn hàng ACTIVE. */
            BigDecimal avgOrderValue
    ) {}

    /** Thống kê doanh thu + lượt bán của một gói subscription. */
    public record PackageRevenueDTO(
            Integer     packageId,
            String      packageName,
            BigDecimal  unitPrice,
            Integer     durationDays,
            long        orderCount,
            BigDecimal  totalRevenue,
            /** Tỉ trọng doanh thu của gói này so với tổng (%). */
            double      revenueShare
    ) {}

    /** Thông tin tóm tắt một đơn hàng gần đây. */
    public record RecentOrderDTO(
            Long        orderId,
            String      buyerName,
            String      buyerEmail,
            String      packageName,
            BigDecimal  amountPaid,
            String      paymentMethod,
            String      status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {}
}
