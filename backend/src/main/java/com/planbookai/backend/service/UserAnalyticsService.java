package com.planbookai.backend.service;

import com.planbookai.backend.dto.UserAnalyticsDTO;
import com.planbookai.backend.dto.UserAnalyticsDTO.*;
import com.planbookai.backend.model.entity.Order;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.ExamRepository;
import com.planbookai.backend.repository.OrderRepository;
import com.planbookai.backend.repository.QuestionBankRepository;
import com.planbookai.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UserAnalyticsService – Tính toán báo cáo người dùng (KAN-26).
 *
 * <h2>Luồng getUserAnalytics()</h2>
 * <ol>
 *   <li>Load tất cả users từ DB.</li>
 *   <li>Load orders để xác định subscription status.</li>
 *   <li>Tính {@code UserSummaryDTO}: tổng, new this month, MoM growth.</li>
 *   <li>Phân nhóm theo role → {@code usersByRole}.</li>
 *   <li>Phân nhóm theo tháng đăng ký năm nay → {@code usersByMonth}.</li>
 *   <li>Tính active/inactive ratio → {@code activeVsInactive}.</li>
 *   <li>Top 5 teacher theo examCount → {@code topTeachers}.</li>
 *   <li>Subscription stats (subscribed / unsubscribed / byPackage) → {@code subscriptionStats}.</li>
 *   <li>10 user mới nhất → {@code recentUsers}.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAnalyticsService {

    private final UserRepository        userRepository;
    private final OrderRepository       orderRepository;
    private final ExamRepository        examRepository;
    private final QuestionBankRepository questionBankRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * [KAN-26] Trả về báo cáo người dùng đầy đủ – chỉ Admin.
     *
     * @return {@link UserAnalyticsDTO} chứa toàn bộ số liệu
     */
    @Transactional(readOnly = true)
    public UserAnalyticsDTO getUserAnalytics() {
        log.info("[UserAnalyticsService] getUserAnalytics()");

        LocalDateTime now = LocalDateTime.now();
        int currentYear   = now.getYear();
        YearMonth thisMonth = YearMonth.now();
        YearMonth lastMonth = thisMonth.minusMonths(1);

        // 1. Load dữ liệu
        List<User>  allUsers     = userRepository.findAllOrderByCreatedAtDesc();
        List<Order> allOrders    = orderRepository.findAllOrdersSorted();

        log.info("[UserAnalyticsService] Total users: {}, Total orders: {}",
                allUsers.size(), allOrders.size());

        // 2. Map userId → active order (nếu có)
        Map<Long, Order> activeOrderByUser = allOrders.stream()
                .filter(o -> Order.OrderStatus.ACTIVE.equals(o.getStatus())
                         && o.getUser() != null)
                .collect(Collectors.toMap(
                        o -> o.getUser().getId(),
                        o -> o,
                        (a, b) -> a));   // giữ order đầu tiên (mới nhất vì đã sort)

        // 3. Kỳ tính toán
        LocalDateTime thisMonthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd   = thisMonth.atEndOfMonth().atTime(23, 59, 59);
        LocalDateTime lastMonthStart = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd   = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // 4. Build từng phần
        UserSummaryDTO      summary          = buildSummary(allUsers, activeOrderByUser,
                                                            thisMonthStart, thisMonthEnd,
                                                            lastMonthStart, lastMonthEnd);
        Map<String, Long>   usersByRole      = buildUsersByRole(allUsers);
        Map<String, Long>   usersByMonth     = buildUsersByMonth(allUsers, currentYear);
        ActiveStatusDTO     activeVsInactive = buildActiveVsInactive(allUsers);
        List<TeacherActivityDTO> topTeachers = buildTopTeachers(allUsers);
        SubscriptionStatsDTO subStats        = buildSubscriptionStats(allUsers, activeOrderByUser);
        List<RecentUserDTO>  recentUsers     = buildRecentUsers(allUsers, activeOrderByUser);

        return new UserAnalyticsDTO(
                now,
                summary,
                usersByRole,
                usersByMonth,
                activeVsInactive,
                topTeachers,
                subStats,
                recentUsers
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private UserSummaryDTO buildSummary(
            List<User> allUsers,
            Map<Long, Order> activeOrderByUser,
            LocalDateTime thisMonthStart, LocalDateTime thisMonthEnd,
            LocalDateTime lastMonthStart, LocalDateTime lastMonthEnd) {

        long total    = allUsers.size();
        long active   = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long inactive = total - active;

        long newThisMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null
                        && !u.getCreatedAt().isBefore(thisMonthStart)
                        && !u.getCreatedAt().isAfter(thisMonthEnd))
                .count();

        long newLastMonth = allUsers.stream()
                .filter(u -> u.getCreatedAt() != null
                        && !u.getCreatedAt().isBefore(lastMonthStart)
                        && !u.getCreatedAt().isAfter(lastMonthEnd))
                .count();

        Double growthRate = null;
        if (newLastMonth > 0) {
            growthRate = ((double)(newThisMonth - newLastMonth) / newLastMonth) * 100.0;
            growthRate = Math.round(growthRate * 100.0) / 100.0;
        }

        long teachers = countByRole(allUsers, Role.RoleName.TEACHER);
        long managers = countByRole(allUsers, Role.RoleName.MANAGER);
        long admins   = countByRole(allUsers, Role.RoleName.ADMIN);
        long staff    = countByRole(allUsers, Role.RoleName.STAFF);
        long subscribed = activeOrderByUser.size();

        return new UserSummaryDTO(
                total, active, inactive,
                newThisMonth, newLastMonth, growthRate,
                teachers, managers, admins, staff,
                subscribed
        );
    }

    /** Số user theo role (keys: ADMIN, MANAGER, STAFF, TEACHER). */
    private Map<String, Long> buildUsersByRole(List<User> allUsers) {
        // Khởi tạo đầy đủ 4 role với giá trị 0
        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        result.put("ADMIN",   0L);
        result.put("MANAGER", 0L);
        result.put("STAFF",   0L);
        result.put("TEACHER", 0L);

        allUsers.forEach(u -> {
            if (u.getRole() != null && u.getRole().getName() != null) {
                result.merge(u.getRole().getName().name(), 1L, Long::sum);
            }
        });
        return result;
    }

    /** Số user đăng ký mới theo tháng trong năm {@code year} (12 buckets). */
    private Map<String, Long> buildUsersByMonth(List<User> allUsers, int year) {
        LinkedHashMap<String, Long> monthly = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthly.put(String.format("%d-%02d", year, m), 0L);
        }
        allUsers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().getYear() == year)
                .forEach(u -> {
                    String key = u.getCreatedAt().format(MONTH_FMT);
                    monthly.merge(key, 1L, Long::sum);
                });
        return monthly;
    }

    /** Tỉ lệ active / inactive. */
    private ActiveStatusDTO buildActiveVsInactive(List<User> allUsers) {
        long active   = allUsers.stream().filter(u -> Boolean.TRUE.equals(u.getIsActive())).count();
        long inactive = allUsers.size() - active;
        double total  = allUsers.isEmpty() ? 1 : allUsers.size();
        return new ActiveStatusDTO(
                active,   inactive,
                round2(active   / total * 100),
                round2(inactive / total * 100)
        );
    }

    /** Top 5 teacher theo số đề thi. */
    private List<TeacherActivityDTO> buildTopTeachers(List<User> allUsers) {
        return allUsers.stream()
                .filter(u -> u.getRole() != null
                        && Role.RoleName.TEACHER.equals(u.getRole().getName()))
                .map(teacher -> {
                    // ExamRepository.findByTeacherId không phân trang → dùng Page unpaged
                    long examCount = examRepository
                            .findByTeacherId(teacher.getId(),
                                    org.springframework.data.domain.Pageable.unpaged())
                            .getTotalElements();

                    long publishedCount = examRepository
                            .findByTeacherWithFilters(
                                    teacher.getId(), null,
                                    com.planbookai.backend.model.entity.Exam.ExamStatus.PUBLISHED,
                                    org.springframework.data.domain.Pageable.unpaged())
                            .getTotalElements();

                    long bankCount = questionBankRepository
                            .findByCreatedById(teacher.getId()).size();

                    return new TeacherActivityDTO(
                            teacher.getId(),
                            teacher.getFullName(),
                            teacher.getEmail(),
                            examCount,
                            publishedCount,
                            bankCount,
                            teacher.getCreatedAt()   // proxy lastActivity = join date
                    );
                })
                .sorted(Comparator.comparingLong(TeacherActivityDTO::examCount).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /** Subscription stats: subscribed / unsubscribed / by package. */
    private SubscriptionStatsDTO buildSubscriptionStats(
            List<User> allUsers, Map<Long, Order> activeOrderByUser) {

        long subscribed   = activeOrderByUser.size();
        long unsubscribed = allUsers.size() - subscribed;
        double total      = allUsers.isEmpty() ? 1 : allUsers.size();

        // Count by package name
        Map<String, Long> byPackage = activeOrderByUser.values().stream()
                .collect(Collectors.groupingBy(
                        o -> o.getSubscriptionPackage() != null
                             && o.getSubscriptionPackage().getName() != null
                             ? o.getSubscriptionPackage().getName()
                             : "Không xác định",
                        Collectors.counting()));

        return new SubscriptionStatsDTO(
                subscribed, unsubscribed,
                round2(subscribed / total * 100),
                byPackage
        );
    }

    /** 10 user mới nhất (đã sort rồi). */
    private List<RecentUserDTO> buildRecentUsers(
            List<User> allUsers, Map<Long, Order> activeOrderByUser) {

        return allUsers.stream()
                .limit(10)
                .map(u -> {
                    Order activeOrder = activeOrderByUser.get(u.getId());
                    String pkgName = activeOrder != null
                            && activeOrder.getSubscriptionPackage() != null
                            ? activeOrder.getSubscriptionPackage().getName()
                            : null;
                    String roleName = u.getRole() != null && u.getRole().getName() != null
                            ? u.getRole().getName().name() : null;
                    return new RecentUserDTO(
                            u.getId(), u.getFullName(), u.getEmail(),
                            roleName, u.getIsActive(), u.getEmailVerified(),
                            pkgName, u.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private static long countByRole(List<User> users, Role.RoleName role) {
        return users.stream()
                .filter(u -> u.getRole() != null && role.equals(u.getRole().getName()))
                .count();
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
