package com.planbookai.backend.controller;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanGenerateRequest;
import com.planbookai.backend.service.LessonPlanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/lesson-plans")
public class LessonPlanController {

    private final LessonPlanService service;

    public LessonPlanController(LessonPlanService service) {
        this.service = service;
    }

    /**
     * Sinh giáo án bằng AI.
     * Nếu saveToDb=true → lưu vào DB và trả về LessonPlanDTO có id.
     * Nếu saveToDb=false → chỉ trả về preview, không lưu.
     *
     * POST /api/v1/ai/lesson-plans/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<LessonPlanDTO> generate(@Valid @RequestBody LessonPlanGenerateRequest request) {
        LessonPlanDTO result = service.generateLessonPlan(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Lấy tất cả giáo án đã lưu của current user.
     *
     * GET /api/v1/ai/lesson-plans
     */
    @GetMapping
    public ResponseEntity<List<LessonPlanDTO>> getAll() {
        List<LessonPlanDTO> all = service.getAllLessonPlans();
        return ResponseEntity.ok(all);
    }

    /**
     * Lấy giáo án theo ID.
     *
     * GET /api/v1/ai/lesson-plans/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LessonPlanDTO> getById(@PathVariable Long id) {
        return service.getLessonPlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
