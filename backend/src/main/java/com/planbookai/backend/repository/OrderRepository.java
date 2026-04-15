package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository truy cập dữ liệu cho bảng orders.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Lấy tất cả order của một user. */
    List<Order> findByUserId(Long userId);

    // ── Revenue analytics queries (KAN-26) ────────────────────────────────────

    /**
     * Tổng doanh thu của các order ACTIVE trong khoảng thời gian.
     * Trả về 0 nếu không có order nào (COALESCE).
     */
    @Query("""
            select coalesce(sum(o.amountPaid), 0)
            from Order o
            where o.status = 'ACTIVE'
              and o.createdAt between :from and :to
            """)
    BigDecimal sumRevenueByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /**
     * Tổng doanh thu toàn thời gian (chỉ ACTIVE).
     */
    @Query("select coalesce(sum(o.amountPaid), 0) from Order o where o.status = 'ACTIVE'")
    BigDecimal sumTotalRevenue();

    /**
     * Tất cả order ACTIVE – dùng để group by tháng/package ở Java layer.
     * Sắp xếp mới nhất trước.
     */
    @Query("select o from Order o where o.status = 'ACTIVE' order by o.createdAt desc")
    List<Order> findAllActiveOrders();

    /**
     * Toàn bộ orders (mọi trạng thái), sắp xếp mới nhất trước.
     * Dùng cho recent orders và ordersByStatus.
     */
    @Query("select o from Order o order by o.createdAt desc")
    List<Order> findAllOrdersSorted();

    /**
     * Số người dùng có ít nhất 1 order ACTIVE (unique buyers).
     */
    @Query("select count(distinct o.user.id) from Order o where o.status = 'ACTIVE'")
    long countUniqueBuyers();
}
