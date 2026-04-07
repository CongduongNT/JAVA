package com.planbookai.backend.controller;

import com.planbookai.backend.dto.CurriculumFrameworkDTO;
import com.planbookai.backend.dto.CurriculumFrameworkRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.CurriculumFrameworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/frameworks")
@RequiredArgsConstructor
public class CurriculumFrameworkController {

    private final CurriculumFrameworkService frameworkService;

    /**
     * GET /api/v1/frameworks
     * Lấy danh sách frameworks (Admin: tất cả, Others: chỉ published)
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<PageResponse<CurriculumFrameworkDTO>> getFrameworks(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String gradeLevel,
            @AuthenticationPrincipal User user) {
        
        // Admin có thể xem tất cả, các role khác chỉ xem published
        if (isAdmin(user)) {
            return ResponseEntity.ok(frameworkService.getAllFrameworks(page, size, subject, gradeLevel));
        } else {
            return ResponseEntity.ok(frameworkService.getPublishedFrameworks(page, size, subject, gradeLevel));
        }
    }

    /**
     * GET /api/v1/frameworks/published
     * Lấy danh sách tất cả frameworks đã publish (public endpoint)
     */
    @GetMapping("/published")
    public ResponseEntity<List<CurriculumFrameworkDTO>> getPublishedFrameworks() {
        return ResponseEntity.ok(frameworkService.getAllPublishedFrameworks());
    }

    /**
     * GET /api/v1/frameworks/{id}
     * Lấy chi tiết framework theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STAFF','MANAGER','ADMIN')")
    public ResponseEntity<CurriculumFrameworkDTO> getFramework(@PathVariable Integer id) {
        return ResponseEntity.ok(frameworkService.getFrameworkById(id));
    }

    /**
     * POST /api/v1/frameworks
     * Tạo mới framework (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurriculumFrameworkDTO> createFramework(
            @Valid @RequestBody CurriculumFrameworkRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(frameworkService.createFramework(request, user));
    }

    /**
     * PUT /api/v1/frameworks/{id}
     * Cập nhật framework (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurriculumFrameworkDTO> updateFramework(
            @PathVariable Integer id,
            @Valid @RequestBody CurriculumFrameworkRequest request) {
        return ResponseEntity.ok(frameworkService.updateFramework(id, request));
    }

    /**
     * DELETE /api/v1/frameworks/{id}
     * Xóa framework (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFramework(@PathVariable Integer id) {
        frameworkService.deleteFramework(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/frameworks/{id}/publish
     * Publish framework (Admin only)
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurriculumFrameworkDTO> publishFramework(@PathVariable Integer id) {
        return ResponseEntity.ok(frameworkService.publishFramework(id, true));
    }

    /**
     * PUT /api/v1/frameworks/{id}/unpublish
     * Unpublish framework (Admin only)
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurriculumFrameworkDTO> unpublishFramework(@PathVariable Integer id) {
        return ResponseEntity.ok(frameworkService.publishFramework(id, false));
    }

    private boolean isAdmin(User user) {
        return user != null && 
               user.getRole() != null && 
               user.getRole().getName().name().equals("ADMIN");
    }
}