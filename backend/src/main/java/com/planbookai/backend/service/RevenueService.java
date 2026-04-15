package com.planbookai.backend.service;

import com.planbookai.backend.dto.RevenueAnalyticsDTO;
import com.planbookai.backend.dto.RevenueAnalyticsDTO.*;
import com.planbookai.backend.model.entity.Order;
import com.planbookai.backend.model.entity.SubscriptionPackage;
import com.planbookai.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RevenueService – Tính toán báo cáo doanh thu hệ thống (KAN-26).
 *
 * <h2>Luồng getRevenueAnalytics()</h2>
 * <ol>
 *   <li>Load toàn bộ orders (mọi status) từ DB.</li>
 *   <li>Tính {@code RevenueSummaryDTO}: tổng, tháng này, tháng trước, MoM growth.</li>
 *   <li>Group ACTIVE orders theo tháng → {@code revenueByMonth} (12 tháng năm hiện tại).</li>
 *   <li>Group ACTIVE orders theo package → {@code revenueByPackage} & {@code topPackages}.</li>
 *   <li>Group tất cả orders theo status → {@code ordersByStatus}.</li>
 *   <li>Group theo payment_method → {@code paymentMethodStats}.</li>
 *   <li>Lấy 10 order gần nhất → {@code recentOrders}.</li>
 * </ol>
 *
 * <p><strong>Nguồn dữ liệu:</strong> Bảng {@code orders} và {@code subscription_packages}.
 * Chỉ tính doanh thu từ orders có {@code status = ACTIVE}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueService {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * [KAN-26] Trả về báo cáo doanh thu đầy đủ cho Admin/Manager.
     *
     * @return {@link RevenueAnalyticsDTO} chứa toàn bộ số liệu doanh thu
     */
    @Transactional(readOnly = true)
    public RevenueAnalyticsDTO getRevenueAnalytics() {
        log.info("[RevenueService] getRevenueAnalytics()");

        // 1. Load dữ liệu từ DB
        List<Order> allOrders    = orderRepository.findAllOrdersSorted();
        List<Order> activeOrders = allOrders.stream()
                .filter(o -> Order.OrderStatus.ACTIVE.equals(o.getStatus()))
                .collect(Collectors.toList());

        log.info("[RevenueService] Total orders: {}, ACTIVE: {}", allOrders.size(), activeOrders.size());

        // 2. Kỳ tính toán
        LocalDateTime now      = LocalDateTime.now();
        YearMonth     thisMonth = YearMonth.now();
        YearMonth     lastMonth = thisMonth.minusMonths(1);

        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd   = thisMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd   = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // 3. Summary
        RevenueSummaryDTO summary = buildSummary(
                allOrders, activeOrders,
                thisMonthStart, thisMonthEnd,
                lastMonthStart, lastMonthEnd,
                thisMonth.getYear());

        // 4. Revenue by month (12 tháng trong năm hiện tại)
        Map<String, BigDecimal> revenueByMonth = buildRevenueByMonth(activeOrders, thisMonth.getYear());

        // 5. Revenue by package
        Map<String, BigDecimal> revenueByPackage = buildRevenueByPackage(activeOrders);

        // 6. Orders by status
        Map<String, Long> ordersByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getStatus() != null ? o.getStatus().name() : "UNKNOWN",
                        Collectors.counting()));

        // 7. Top packages
        List<PackageRevenueDTO> topPackages = buildTopPackages(activeOrders, summary.totalRevenue());

        // 8. Payment method stats (mọi trạng thái)
        Map<String, Long> paymentMethodStats = allOrders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getPaymentMethod() != null ? o.getPaymentMethod() : "Không xác định",
                        Collectors.counting()));

        // 9. Recent orders (tối đa 10, mọi status, mới nhất trước)
        List<RecentOrderDTO> recentOrders = allOrders.stream()
                .limit(10)
                .map(this::toRecentOrderDTO)
                .collect(Collectors.toList());

        return new RevenueAnalyticsDTO(
                now,
                summary,
                revenueByMonth,
                revenueByPackage,
                ordersByStatus,
                topPackages,
                paymentMethodStats,
                recentOrders
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private RevenueSummaryDTO buildSummary(
            List<Order> allOrders,
            List<Order> activeOrders,
            LocalDateTime thisMonthStart, LocalDateTime thisMonthEnd,
            LocalDateTime lastMonthStart, LocalDateTime lastMonthEnd,
            int currentYear) {

        // Tổng doanh thu
        BigDecimal totalRevenue = activeOrders.stream()
                .map(o -> o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tháng này
        BigDecimal currentMonthRevenue = activeOrders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && !o.getCreatedAt().isBefore(thisMonthStart)
                        && !o.getCreatedAt().isAfter(thisMonthEnd))
                .map(o -> o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tháng trước
        BigDecimal lastMonthRevenue = activeOrders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && !o.getCreatedAt().isBefore(lastMonthStart)
                        && !o.getCreatedAt().isAfter(lastMonthEnd))
                .map(o -> o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // MoM Growth
        Double momGrowth = null;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            momGrowth = currentMonthRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        // Năm hiện tại
        LocalDateTime yearStart = LocalDate.of(currentYear, 1, 1).atStartOfDay();
        LocalDateTime yearEnd   = LocalDate.of(currentYear, 12, 31).atTime(23, 59, 59);
        BigDecimal currentYearRevenue = activeOrders.stream()
                .filter(o -> o.getCreatedAt() != null
                        && !o.getCreatedAt().isBefore(yearStart)
                        && !o.getCreatedAt().isAfter(yearEnd))
                .map(o -> o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Order counts
        long activeCount  = activeOrders.size();
        long pendingCount = allOrders.stream()
                .filter(o -> Order.OrderStatus.PENDING.equals(o.getStatus())).count();

        // Unique buyers
        long uniqueBuyers = activeOrders.stream()
                .filter(o -> o.getUser() != null)
                .map(o -> o.getUser().getId())
                .distinct().count();

        // Avg order value
        BigDecimal avgOrderValue = activeOrders.isEmpty()
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(activeOrders.size()), 2, RoundingMode.HALF_UP);

        return new RevenueSummaryDTO(
                totalRevenue,
                currentMonthRevenue,
                lastMonthRevenue,
                momGrowth,
                currentYearRevenue,
                allOrders.size(),
                activeCount,
                pendingCount,
                uniqueBuyers,
                avgOrderValue
        );
    }

    /**
     * Doanh thu theo từng tháng trong năm {@code year}.
     * Đảm bảo tất cả 12 tháng đều có mặt trong map (giá trị = 0 nếu không có đơn).
     */
    private Map<String, BigDecimal> buildRevenueByMonth(List<Order> activeOrders, int year) {
        // Khởi tạo 12 tháng với giá trị = 0
        LinkedHashMap<String, BigDecimal> monthly = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthly.put(String.format("%d-%02d", year, m), BigDecimal.ZERO);
        }

        // Cộng vào từng tháng
        activeOrders.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().getYear() == year)
                .forEach(o -> {
                    String key = o.getCreatedAt().format(MONTH_FMT);
                    BigDecimal amt = o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO;
                    monthly.merge(key, amt, BigDecimal::add);
                });

        return monthly;
    }

    /** Doanh thu theo tên gói subscription. */
    private Map<String, BigDecimal> buildRevenueByPackage(List<Order> activeOrders) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        activeOrders.forEach(o -> {
            String pkgName = getPackageName(o);
            BigDecimal amt = o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO;
            result.merge(pkgName, amt, BigDecimal::add);
        });
        // Sắp xếp giảm dần theo doanh thu
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Top N gói subscription theo doanh thu. */
    private List<PackageRevenueDTO> buildTopPackages(List<Order> activeOrders, BigDecimal totalRevenue) {
        // Group by package id
        Map<Integer, List<Order>> byPkg = activeOrders.stream()
                .filter(o -> o.getSubscriptionPackage() != null)
                .collect(Collectors.groupingBy(o -> o.getSubscriptionPackage().getId()));

        return byPkg.entrySet().stream()
                .map(e -> {
                    SubscriptionPackage pkg = e.getValue().get(0).getSubscriptionPackage();
                    BigDecimal pkgRevenue = e.getValue().stream()
                            .map(o -> o.getAmountPaid() != null ? o.getAmountPaid() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    double share = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? pkgRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                       .multiply(BigDecimal.valueOf(100))
                                       .setScale(2, RoundingMode.HALF_UP)
                                       .doubleValue()
                            : 0.0;
                    return new PackageRevenueDTO(
                            pkg.getId(),
                            pkg.getName(),
                            pkg.getPrice(),
                            pkg.getDurationDays(),
                            e.getValue().size(),
                            pkgRevenue,
                            share
                    );
                })
                .sorted(Comparator.comparing(PackageRevenueDTO::totalRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private RecentOrderDTO toRecentOrderDTO(Order o) {
        return new RecentOrderDTO(
                o.getId(),
                o.getUser()  != null ? o.getUser().getFullName() : null,
                o.getUser()  != null ? o.getUser().getEmail()    : null,
                getPackageName(o),
                o.getAmountPaid(),
                o.getPaymentMethod(),
                o.getStatus() != null ? o.getStatus().name() : null,
                o.getCreatedAt(),
                o.getExpiresAt()
        );
    }

    private static String getPackageName(Order o) {
        return o.getSubscriptionPackage() != null
               && o.getSubscriptionPackage().getName() != null
                ? o.getSubscriptionPackage().getName()
                : "Không xác định";
    }
}
