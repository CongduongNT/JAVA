package com.planbookai.backend.controller;

import com.planbookai.backend.dto.*;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AiPromptTemplateService;
import com.planbookai.backend.service.LessonPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/lesson-plans")
@Tag(name = "Lesson Plans", description = "Quản lý giáo án mẫu (Dành cho Manager và Admin)")
@RequiredArgsConstructor
public class LessonPlanController {

    private final AiPromptTemplateService promptTemplateService;
    private final LessonPlanService lessonPlanService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Lấy danh sách giáo án của tôi", description = "Cho phép giáo viên xem danh sách giáo án cá nhân.")
    public ResponseEntity<PageResponse<LessonPlanListItemDTO>> getMyLessonPlans(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.getMyLessonPlans(user, page, size, status, subject, gradeLevel, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Tạo giáo án mới")
    public ResponseEntity<LessonPlanDTO> createLessonPlan(
            @RequestBody LessonPlanRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lessonPlanService.createLessonPlan(request, user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Xem chi tiết giáo án")
    public ResponseEntity<LessonPlanDTO> getLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.getLessonPlan(id, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Cập nhật giáo án")
    public ResponseEntity<LessonPlanDTO> updateLessonPlan(
            @PathVariable Long id,
            @RequestBody LessonPlanRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.updateLessonPlan(id, request, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Xóa giáo án")
    public ResponseEntity<Void> deleteLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        lessonPlanService.deleteLessonPlan(id, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Công khai giáo án")
    public ResponseEntity<LessonPlanDTO> publishLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.publishLessonPlan(id, user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Lấy danh sách giáo án mẫu", description = "Cho phép Manager lọc giáo án do Staff tạo (thường là để phê duyệt).")
    public ResponseEntity<List<PromptTemplateDTO>> getLessonPlans(
            @RequestParam(value = "created_by_staff", required = false) Boolean createdByStaff,
            @RequestParam(value = "status", required = false) String status) {
        
        // Mặc định lọc theo mục đích tạo giáo án
        String purpose = "LESSON_PLAN_GEN";
        
        Role.RoleName filterRole = null;
        if (Boolean.TRUE.equals(createdByStaff)) {
            filterRole = Role.RoleName.STAFF;
        }

        // Manager có thể truyền status=PENDING để xem những bản cần duyệt ngay
        List<PromptTemplateDTO> results = promptTemplateService.getTemplatesByFilter(
                filterRole, 
                purpose, 
                status);
                
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Phê duyệt hoặc từ chối giáo án", description = "Manager ra quyết định chuyển trạng thái giáo án từ PENDING sang APPROVED hoặc REJECTED.")
    public ResponseEntity<PromptTemplateDTO> approveLessonPlan(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User manager) {
        String status = body.get("status");
        return ResponseEntity.ok(promptTemplateService.approveTemplate(id, status, manager));
    }
}
