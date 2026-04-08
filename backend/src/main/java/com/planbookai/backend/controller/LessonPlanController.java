package com.planbookai.backend.controller;

import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.LessonPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lesson-plans")
@RequiredArgsConstructor
@Tag(name = "Lesson Plans", description = "Teacher lesson plan management")
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;

    @GetMapping
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
}
