package com.planbookai.backend.controller;

import com.planbookai.backend.dto.OrderDTO;
import com.planbookai.backend.dto.OrderRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final SubscriptionService subscriptionService;

    public OrderController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /*
     * [MANAGER, ADMIN] Lấy tất cả orders
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(subscriptionService.getAllOrders());
    }

    /*
     * [MANAGER, ADMIN] Phê duyệt / huỷ đơn hàng
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(subscriptionService.updateOrderStatus(id, status));
    }

    /*
     * [TEACHER] Mua gói (Tạo order mới)
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<OrderDTO> createOrder(
            @RequestBody OrderRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.createOrder(request, user));
    }

    /*
     * [TEACHER] Xem các đơn hàng đã mua
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.getMyOrders(user));
    }
}
