package com.planbookai.backend.controller;

import com.planbookai.backend.dto.SystemConfigDTO;
import com.planbookai.backend.dto.UpdateSystemConfigRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SystemConfigController – Cấu hình hệ thống toàn cục.
 * Chỉ Admin có quyền đọc và cập nhật.
 *
 * <p>Base URL: /api/v1/admin/system-config
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET  /admin/system-config       – Lấy tất cả cấu hình</li>
 *   <li>GET  /admin/system-config/{key} – Lấy 1 cấu hình theo key</li>
 *   <li>PUT  /admin/system-config/{key} – Cập nhật giá trị cấu hình</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/admin/system-config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "System Config", description = "Cấu hình hệ thống toàn cục – chỉ Admin")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "Lấy tất cả cấu hình hệ thống")
    public ResponseEntity<List<SystemConfigDTO>> getAll() {
        return ResponseEntity.ok(systemConfigService.getAll());
    }

    @GetMapping("/{key}")
    @Operation(summary = "Lấy cấu hình theo key")
    public ResponseEntity<SystemConfigDTO> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(systemConfigService.getByKey(key));
    }

    @PutMapping("/{key}")
    @Operation(
            summary = "Cập nhật giá trị cấu hình",
            description = "Chỉ cập nhật `configValue`. Key không thể thay đổi."
    )
    public ResponseEntity<SystemConfigDTO> update(
            @PathVariable String key,
            @Valid @RequestBody UpdateSystemConfigRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(systemConfigService.update(key, request, admin.getEmail()));
    }
}
