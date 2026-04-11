package com.planbookai.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.model.entity.LessonPlan;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.util.PromptBuilder;
import com.planbookai.backend.util.PromptBuilder.LessonFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final Client geminiClient;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiAIService(Client geminiClient, PromptBuilder promptBuilder) {
        this.geminiClient = geminiClient;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();
    }

    public List<QuestionDTO> generateQuestions(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int count) {

        String prompt = promptBuilder.buildQuestionPrompt(subject, topic, difficulty, type, count);
        log.info("[GeminiAI] Sending prompt for {} questions: subject={}, topic={}, difficulty={}, type={}",
                count, subject, topic, difficulty, type);

        String rawResponse;
        try {
            GenerateContentResponse response = geminiClient.models.generateContent(model, prompt, null);
            rawResponse = response != null ? response.text() : null;
        } catch (Exception e) {
            log.error("[GeminiAI] API call failed: {}", e.getMessage(), e);
            throw new AIServiceException("Gemini AI service is unavailable: " + e.getMessage());
        }

        log.debug("[GeminiAI] Raw response: {}", rawResponse);

        String cleanedJson = cleanJsonResponse(rawResponse);

        List<Map<String, Object>> rawQuestions;
        try {
            rawQuestions = objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            log.error("[GeminiAI] Failed to parse JSON response: {}", cleanedJson, e);
            throw new AIServiceException("AI returned invalid JSON. Please try again.");
        }

        return rawQuestions.stream()
                .map(raw -> mapRawToDTO(raw, subject, topic, difficulty, type))
                .collect(Collectors.toList());
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) {
            return "[]";
        }

        String trimmed = raw.trim();

        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1).trim();
            }
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
        }

        return trimmed;
    }

    @SuppressWarnings("unchecked")
    private QuestionDTO mapRawToDTO(
            Map<String, Object> raw,
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type) {

        String content = getStr(raw, "content", "");
        String correctAnswer = getStr(raw, "correctAnswer", "");
        String explanation = getStr(raw, "explanation", "");
        String topicVal = getStr(raw, "topic", topic);

        Question.QuestionType parsedType = parseEnum(
                Question.QuestionType.class,
                getStr(raw, "type", type.name()),
                type
        );

        Question.Difficulty parsedDifficulty = parseEnum(
                Question.Difficulty.class,
                getStr(raw, "difficulty", difficulty.name()),
                difficulty
        );

        List<Map<String, Object>> options = null;
        Object rawOptions = raw.get("options");

        if (rawOptions instanceof List<?>) {
            List<?> list = (List<?>) rawOptions;
            if (!list.isEmpty()) {
                options = new ArrayList<Map<String, Object>>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?>) {
                        options.add((Map<String, Object>) item);
                    }
                }
            }
        }

        return QuestionDTO.builder()
                .content(content)
                .type(parsedType.name())
                .difficulty(parsedDifficulty.name())
                .topic(topicVal)
                .options(options)
                .correctAnswer(correctAnswer)
                .explanation(explanation)
                .aiGenerated(true)
                .isApproved(false)
                .build();
    }

    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultVal) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public LessonPlanDTO generateLessonPlan(
            String subject,
            String topic,
            String grade,
            int duration,
            LessonFramework framework,
            String objectives) {

        String prompt = promptBuilder.buildLessonPlanPrompt(
                subject, topic, grade, duration, framework, objectives);

        log.info("[GeminiAI] Generating lesson plan: subject={}, topic={}, grade={}, duration={}, framework={}",
                subject, topic, grade, duration, framework.label());

        String rawResponse;
        try {
            GenerateContentResponse response = geminiClient.models.generateContent(model, prompt, null);
            rawResponse = response != null ? response.text() : null;
        } catch (Exception e) {
            log.error("[GeminiAI] API call failed: {}", e.getMessage(), e);
            throw new AIServiceException("Gemini AI service is unavailable: " + e.getMessage());
        }

        log.debug("[GeminiAI] Raw lesson plan response: {}", rawResponse);

        String cleanedJson = cleanJsonResponse(rawResponse);

        Map<String, Object> raw;
        try {
            raw = objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
            log.error("[GeminiAI] Failed to parse lesson plan JSON: {}", cleanedJson, e);
            throw new AIServiceException("AI returned invalid JSON for lesson plan. Please try again.");
        }

        if (raw == null) {
            log.error("[GeminiAI] Raw lesson plan map is null");
            throw new AIServiceException("AI returned empty response for lesson plan.");
        }

        return mapRawToLessonPlan(raw);
    }

    private LessonPlanDTO mapRawToLessonPlan(Map<String, Object> raw) {
        return LessonPlanDTO.builder()
                .title(getStr(raw, "title", ""))
                .gradeLevel(getStr(raw, "grade_level", ""))
                .subject(getStr(raw, "subject", ""))
                .topic(getStr(raw, "topic", ""))
                .durationMinutes(getInt(raw, "duration_minutes", 0))
                .lessonObjectives(getStrList(raw, "objectives"))
                .materialItems(getStrList(raw, "materials"))
                .lessonFlow(getLessonPhases(raw.get("lesson_flow")))
                .assessmentDetail(getAssessment(raw.get("assessment")))
                .homework(getStr(raw, "homework", ""))
                .notes(getStr(raw, "notes", ""))
                .aiGenerated(true)
                .status(LessonPlan.LessonPlanStatus.DRAFT)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<LessonPlanDTO.LessonPhase> getLessonPhases(Object raw) {
        if (!(raw instanceof List<?>)) {
            return Collections.emptyList();
        }

        List<?> list = (List<?>) raw;

        return list.stream()
                .filter(item -> item instanceof Map)
                .map(item -> (Map<String, Object>) item)
                .map(map -> LessonPlanDTO.LessonPhase.builder()
                        .phase(getStr(map, "phase", ""))
                        .timeMinutes(getInt(map, "time_minutes", 0))
                        .activities(getStr(map, "activities", ""))
                        .teacherActions(getStr(map, "teacher_actions", ""))
                        .studentActions(getStr(map, "student_actions", ""))
                        .build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private LessonPlanDTO.AssessmentDetail getAssessment(Object raw) {
        if (!(raw instanceof Map<?, ?>)) {
            return LessonPlanDTO.AssessmentDetail.builder()
                    .methods(Collections.emptyList())
                    .criteria("")
                    .build();
        }

        Map<String, Object> map = (Map<String, Object>) raw;

        return LessonPlanDTO.AssessmentDetail.builder()
                .methods(getStrList(map, "methods"))
                .criteria(getStr(map, "criteria", ""))
                .build();
    }

    private List<String> getStrList(Map<String, Object> raw, String key) {
        Object val = raw.get(key);
        if (val instanceof List<?>) {
            List<?> list = (List<?>) val;
            return list.stream()
                    .filter(v -> v != null)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private int getInt(Map<String, Object> raw, String key, int defaultVal) {
        Object val = raw.get(key);

        if (val instanceof Number) {
            return ((Number) val).intValue();
        }

        if (val instanceof String) {
            try {
                return Integer.parseInt((String) val);
            } catch (Exception ignored) {
            }
        }

        return defaultVal;
    }
}
