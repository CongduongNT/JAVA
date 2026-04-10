package com.planbookai.backend.service;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanGenerateRequest;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.util.PromptBuilder;
import com.planbookai.backend.util.PromptBuilder.LessonFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LessonPlanService {

    private static final Logger log = LoggerFactory.getLogger(LessonPlanService.class);

    private final GeminiAIService geminiAIService;
    
    private final PromptBuilder promptBuilder;

    public LessonPlanService(GeminiAIService geminiAIService, PromptBuilder promptBuilder) {
        this.geminiAIService = geminiAIService;
        this.promptBuilder = promptBuilder;
    }

    /**
     * Sinh giáo án bằng Gemini AI.
     *
     * @param request Thông tin bài học từ client
     * @return LessonPlanDTO chưa lưu DB (dùng cho preview)
     * @throws AIServiceException nếu AI lỗi hoặc framework không hợp lệ
     */
    public LessonPlanDTO generateLessonPlan(LessonPlanGenerateRequest request) {
        LessonFramework framework = parseFramework(request.getFramework());
        String objectives = request.getObjectives();

        log.info("[LessonPlanService] Generating lesson plan: subject={}, topic={}, grade={}, duration={}, framework={}",
                request.getSubject(), request.getTopic(), request.getGradeLevel(),
                request.getDurationMinutes(), framework.label());

        return geminiAIService.generateLessonPlan(
                request.getSubject(),
                request.getTopic(),
                request.getGradeLevel(),
                request.getDurationMinutes(),
                framework,
                objectives
        );
    }

    private LessonFramework parseFramework(String frameworkId) {
        if (frameworkId == null || frameworkId.isBlank()) {
            return LessonFramework.E5;
        }
        try {
            return LessonFramework.valueOf(frameworkId.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("[LessonPlanService] Unknown framework '{}', defaulting to E5", frameworkId);
            return LessonFramework.E5;
        }
    }
}
