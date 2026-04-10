package com.planbookai.backend.controller;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanGenerateRequest;
import com.planbookai.backend.service.LessonPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/lesson-plans")
public class LessonPlanController {

    private final LessonPlanService service;

    public LessonPlanController(LessonPlanService service) {
        this.service = service;
    }

    /**
     * Sinh giáo án bằng AI (chỉ trả về preview, chưa lưu DB).
     *
     * POST /api/v1/ai/lesson-plans/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<LessonPlanDTO> generate(@Valid @RequestBody LessonPlanGenerateRequest request) {
        LessonPlanDTO result = service.generateLessonPlan(request);
        return ResponseEntity.ok(result);
    }
}
