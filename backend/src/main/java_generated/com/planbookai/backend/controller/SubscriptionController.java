package com.planbookai.backend.controller;

import com.planbookai.backend.dto.PackageDTO;
import com.planbookai.backend.dto.PackageRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /*
     * [ALL] Lấy danh sách các gói có sẵn
     */
    @GetMapping
    public ResponseEntity<List<PackageDTO>> getAllPackages() {
        return ResponseEntity.ok(subscriptionService.getAllPackages());
    }

    /*
     * [MANAGER, ADMIN] Tạo gói mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PackageDTO> createPackage(
            @RequestBody PackageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(subscriptionService.createPackage(request, user));
    }

    /*
     * [MANAGER, ADMIN] Cập nhật gói
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PackageDTO> updatePackage(
            @PathVariable Integer id,
            @RequestBody PackageRequest request) {
        return ResponseEntity.ok(subscriptionService.updatePackage(id, request));
    }

    /*
     * [MANAGER, ADMIN] Xoá gói (Hủy bỏ/xóa cứng)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer id) {
        subscriptionService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }

    /*
     * [MANAGER, ADMIN] Deactivate / Vô hiệu hóa gói (ẩn khỏi người dùng mua)
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<PackageDTO> deactivatePackage(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionService.deactivatePackage(id));
    }
}
