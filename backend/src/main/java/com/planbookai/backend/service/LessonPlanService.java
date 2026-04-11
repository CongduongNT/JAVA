package com.planbookai.backend.service;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanGenerateRequest;
import com.planbookai.backend.dto.SaveLessonPlanRequest;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.LessonPlan;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.LessonPlanRepository;
import com.planbookai.backend.util.PromptBuilder;
import com.planbookai.backend.util.PromptBuilder.LessonFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LessonPlanService {

    private static final Logger log = LoggerFactory.getLogger(LessonPlanService.class);

    private final GeminiAIService geminiAIService;
    private final LessonPlanRepository lessonPlanRepository;
    private final CurrentUserService currentUserService;
    private final PromptBuilder promptBuilder;

    public LessonPlanService(
            GeminiAIService geminiAIService,
            LessonPlanRepository lessonPlanRepository,
            CurrentUserService currentUserService,
            PromptBuilder promptBuilder) {
        this.geminiAIService = geminiAIService;
        this.lessonPlanRepository = lessonPlanRepository;
        this.currentUserService = currentUserService;
        this.promptBuilder = promptBuilder;
    }

    /**
     * Sinh giáo án bằng Gemini AI.
     *
     * @param request Thông tin bài học từ client
     * @return LessonPlanDTO (có id nếu saveToDb=true)
     * @throws AIServiceException nếu AI lỗi hoặc framework không hợp lệ
     */
    @Transactional
    public LessonPlanDTO generateLessonPlan(LessonPlanGenerateRequest request) {
        LessonFramework framework = parseFramework(request.getFramework());
        String objectives = request.getObjectives();

        log.info("[LessonPlanService] Generating lesson plan: subject={}, topic={}, grade={}, duration={}, framework={}, saveToDb={}",
                request.getSubject(), request.getTopic(), request.getGradeLevel(),
                request.getDurationMinutes(), framework.label(), request.isSaveToDb());

        LessonPlanDTO dto = geminiAIService.generateLessonPlan(
                request.getSubject(),
                request.getTopic(),
                request.getGradeLevel(),
                request.getDurationMinutes(),
                framework,
                objectives
        );

        if (request.isSaveToDb()) {
            User currentUser = currentUserService.getCurrentUserEntity()
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            LessonPlan saved = lessonPlanRepository.save(mapDtoToEntity(dto, currentUser));
            log.info("[LessonPlanService] Lesson plan saved with id={}", saved.getId());
            return mapEntityToDto(saved);
        }

        return dto;
    }

    /**
     * Lưu giáo án đã chỉnh sửa (sau khi user đã review và sửa trong editor).
     * Không gọi AI – chỉ lưu trực tiếp vào DB.
     *
     * @param request Dữ liệu từ editor (đã được user chỉnh sửa)
     * @return LessonPlanDTO có id
     */
    @Transactional
    public LessonPlanDTO saveEditedLessonPlan(SaveLessonPlanRequest request) {
        User currentUser = currentUserService.getCurrentUserEntity()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("[LessonPlanService] Saving edited lesson plan for user={}, topic={}",
                currentUser.getId(), request.getTopic());

        LessonPlanDTO dto = mapSaveRequestToDto(request);
        LessonPlan saved = lessonPlanRepository.save(mapDtoToEntity(dto, currentUser));
        log.info("[LessonPlanService] Edited lesson plan saved with id={}", saved.getId());
        return mapEntityToDto(saved);
    }

    private LessonPlanDTO mapSaveRequestToDto(SaveLessonPlanRequest request) {
        List<LessonPlanDTO.LessonPhase> lessonFlow = null;
        if (request.getLessonFlow() != null) {
            lessonFlow = request.getLessonFlow().stream()
                    .map(phase -> LessonPlanDTO.LessonPhase.builder()
                            .phase(phase.getPhase())
                            .timeMinutes(phase.getTimeMinutes())
                            .activities(phase.getActivities())
                            .teacherActions(phase.getTeacherActions())
                            .studentActions(phase.getStudentActions())
                            .build())
                    .toList();
        }

        LessonPlanDTO.Assessment assessment = null;
        if (request.getAssessment() != null) {
            assessment = LessonPlanDTO.Assessment.builder()
                    .methods(request.getAssessment().getMethods())
                    .criteria(request.getAssessment().getCriteria())
                    .build();
        }

        return LessonPlanDTO.builder()
                .title(request.getTitle() != null ? request.getTitle() : "Untitled")
                .gradeLevel(request.getGradeLevel())
                .subject(request.getSubject())
                .topic(request.getTopic())
                .durationMinutes(request.getDurationMinutes())
                .framework(request.getFramework())
                .objectives(request.getLessonPlanObjectives())
                .materials(request.getMaterials())
                .lessonFlow(lessonFlow)
                .assessment(assessment)
                .homework(request.getHomework())
                .notes(request.getNotes())
                .build();
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

    @Transactional(readOnly = true)
    public List<LessonPlanDTO> getAllLessonPlans() {
        User currentUser = currentUserService.getCurrentUserEntity()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return lessonPlanRepository.findByCreatedById(currentUser.getId())
                .stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LessonPlanDTO> getLessonPlanById(Long id) {
        return lessonPlanRepository.findById(id)
                .map(this::mapEntityToDto);
    }

    private LessonPlan mapDtoToEntity(LessonPlanDTO dto, User creator) {
        List<Map<String, Object>> lessonFlowMaps = null;
        if (dto.getLessonFlow() != null) {
            lessonFlowMaps = dto.getLessonFlow().stream()
                    .map(phase -> {
                        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("phase", phase.getPhase());
                        m.put("timeMinutes", phase.getTimeMinutes());
                        m.put("activities", phase.getActivities());
                        m.put("teacherActions", phase.getTeacherActions());
                        m.put("studentActions", phase.getStudentActions());
                        return m;
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        java.util.Map<String, Object> assessmentMap = null;
        if (dto.getAssessment() != null) {
            assessmentMap = new java.util.LinkedHashMap<>();
            assessmentMap.put("methods", dto.getAssessment().getMethods() != null
                    ? dto.getAssessment().getMethods()
                    : java.util.List.of());
            assessmentMap.put("criteria", dto.getAssessment().getCriteria() != null
                    ? dto.getAssessment().getCriteria()
                    : "");
        }

        return LessonPlan.builder()
                .title(dto.getTitle() != null ? dto.getTitle() : "Untitled")
                .gradeLevel(dto.getGradeLevel())
                .subject(dto.getSubject())
                .topic(dto.getTopic())
                .durationMinutes(dto.getDurationMinutes())
                .framework(dto.getFramework())
                .objectives(dto.getObjectives())
                .materials(dto.getMaterials())
                .lessonFlow(lessonFlowMaps)
                .assessment(assessmentMap)
                .homework(dto.getHomework())
                .notes(dto.getNotes())
                .aiGenerated(true)
                .isApproved(false)
                .createdBy(creator)
                .build();
    }

    @SuppressWarnings("unchecked")
    private LessonPlanDTO mapEntityToDto(LessonPlan entity) {
        LessonPlanDTO.Assessment assessment = null;
        if (entity.getAssessment() != null) {
            Object methods = entity.getAssessment().get("methods");
            Object criteria = entity.getAssessment().get("criteria");
            assessment = LessonPlanDTO.Assessment.builder()
                    .methods(methods instanceof java.util.List
                            ? (java.util.List<String>) methods
                            : java.util.List.of())
                    .criteria(criteria != null ? criteria.toString() : "")
                    .build();
        }

        return LessonPlanDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .gradeLevel(entity.getGradeLevel())
                .subject(entity.getSubject())
                .topic(entity.getTopic())
                .durationMinutes(entity.getDurationMinutes())
                .framework(entity.getFramework())
                .objectives(entity.getObjectives())
                .materials(entity.getMaterials())
                .lessonFlow(entity.getLessonFlow() != null
                        ? entity.getLessonFlow().stream()
                                .filter(m -> m instanceof java.util.Map)
                                .map(m -> (java.util.Map<String, Object>) m)
                                .map(this::mapFlowMapToPhase)
                                .collect(java.util.stream.Collectors.toList())
                        : java.util.List.of())
                .assessment(assessment)
                .homework(entity.getHomework())
                .notes(entity.getNotes())
                .aiGenerated(entity.getAiGenerated())
                .isApproved(entity.getIsApproved())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private LessonPlanDTO.LessonPhase mapFlowMapToPhase(java.util.Map<String, Object> map) {
        return LessonPlanDTO.LessonPhase.builder()
                .phase(map.get("phase") != null ? map.get("phase").toString() : "")
                .timeMinutes(map.get("timeMinutes") != null
                        ? ((Number) map.get("timeMinutes")).intValue()
                        : 0)
                .activities(map.get("activities") != null ? map.get("activities").toString() : "")
                .teacherActions(map.get("teacherActions") != null ? map.get("teacherActions").toString() : "")
                .studentActions(map.get("studentActions") != null ? map.get("studentActions").toString() : "")
                .build();
    }
}
