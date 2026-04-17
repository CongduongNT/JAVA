package com.planbookai.backend.controller;

<<<<<<< HEAD
import com.planbookai.backend.dto.*;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AiPromptTemplateService;
=======
import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanGenerateRequest;
import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.LessonPlanRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.dto.SaveLessonPlanRequest;
import com.planbookai.backend.model.entity.User;
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
import com.planbookai.backend.service.LessonPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
<<<<<<< HEAD
=======
import jakarta.validation.Valid;
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
<<<<<<< HEAD
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
=======
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Lesson Plans", description = "Teacher lesson plan management")
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;

    @GetMapping("/lesson-plans")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(
            summary = "List current teacher lesson plans",
            description = """
                    Returns the current teacher's lesson plans with pagination and lightweight filters.

                    Supported filters:
                    - `status`: DRAFT or PUBLISHED
                    - `subject`
                    - `gradeLevel`
                    - `keyword`: searches in title and topic
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson plans fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only teacher can access this endpoint", content = @Content),
    })
    public ResponseEntity<PageResponse<LessonPlanListItemDTO>> getMyLessonPlans(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size, maximum 100", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Filter by lesson plan status", example = "DRAFT")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by subject keyword", example = "Chemistry")
            @RequestParam(required = false) String subject,
            @Parameter(description = "Filter by grade level keyword", example = "10")
            @RequestParam(required = false) String gradeLevel,
            @Parameter(description = "Search keyword in title or topic", example = "Atomic")
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                lessonPlanService.getMyLessonPlans(user, page, size, status, subject, gradeLevel, keyword));
    }

    @PostMapping("/lesson-plans")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create lesson plan", description = "Creates a new manual lesson plan for the current teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lesson plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only teacher can access this endpoint", content = @Content),
    })
    public ResponseEntity<LessonPlanDTO> createLessonPlan(
            @Valid @RequestBody LessonPlanRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(lessonPlanService.createLessonPlan(request, user));
    }

    @GetMapping("/lesson-plans/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get lesson plan detail", description = "Returns the full detail of a lesson plan owned by the current teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson plan fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only owner teacher can access this lesson plan", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lesson plan not found", content = @Content),
    })
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    public ResponseEntity<LessonPlanDTO> getLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.getLessonPlan(id, user));
    }

<<<<<<< HEAD
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Cập nhật giáo án")
    public ResponseEntity<LessonPlanDTO> updateLessonPlan(
            @PathVariable Long id,
            @RequestBody LessonPlanRequest request,
=======
    @PutMapping("/lesson-plans/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update lesson plan", description = "Updates the editable content of a lesson plan owned by the current teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson plan updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only owner teacher can update this lesson plan", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lesson plan not found", content = @Content),
    })
    public ResponseEntity<LessonPlanDTO> updateLessonPlan(
            @PathVariable Long id,
            @Valid @RequestBody LessonPlanRequest request,
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.updateLessonPlan(id, request, user));
    }

<<<<<<< HEAD
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Xóa giáo án")
=======
    @DeleteMapping("/lesson-plans/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete lesson plan", description = "Deletes a lesson plan owned by the current teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lesson plan deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only owner teacher can delete this lesson plan", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lesson plan not found", content = @Content),
    })
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    public ResponseEntity<Void> deleteLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        lessonPlanService.deleteLessonPlan(id, user);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Công khai giáo án")
=======
    @PutMapping("/lesson-plans/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Publish lesson plan", description = "Publishes a lesson plan owned by the current teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson plan published successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only owner teacher can publish this lesson plan", content = @Content),
            @ApiResponse(responseCode = "404", description = "Lesson plan not found", content = @Content),
    })
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    public ResponseEntity<LessonPlanDTO> publishLessonPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lessonPlanService.publishLessonPlan(id, user));
    }

<<<<<<< HEAD
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
=======
    @PostMapping("/ai/lesson-plans/generate")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Generate lesson plan with AI")
    public ResponseEntity<LessonPlanDTO> generate(@Valid @RequestBody LessonPlanGenerateRequest request) {
        return ResponseEntity.ok(lessonPlanService.generateLessonPlan(request));
    }

    @PostMapping("/ai/lesson-plans/save")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Save edited AI lesson plan")
    public ResponseEntity<LessonPlanDTO> save(@Valid @RequestBody SaveLessonPlanRequest request) {
        return ResponseEntity.ok(lessonPlanService.saveEditedLessonPlan(request));
    }

    @GetMapping("/ai/lesson-plans")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "List current teacher AI lesson plans")
    public ResponseEntity<List<LessonPlanDTO>> getAllAiLessonPlans() {
        return ResponseEntity.ok(lessonPlanService.getAllLessonPlans());
    }

    @GetMapping("/ai/lesson-plans/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get AI lesson plan detail")
    public ResponseEntity<LessonPlanDTO> getAiLessonPlanById(@PathVariable Long id) {
        return lessonPlanService.getLessonPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
>>>>>>> e109ff8b3817c1be84ab73e4b9730312014b9eff
    }
}
