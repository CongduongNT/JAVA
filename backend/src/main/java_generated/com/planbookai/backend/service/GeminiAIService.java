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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final Client geminiClient;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiAIService(Optional<Client> geminiClient, PromptBuilder promptBuilder, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient.orElse(null);
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // Question Generation
    // =========================================================================

    /**
     * Sinh danh sách câu hỏi thông qua Gemini AI (chưa lưu DB).
     */
    public List<QuestionDTO> generateQuestions(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int count) {

        if (geminiClient == null) {
            throw new AIServiceException("Hệ thống AI chưa được thiết lập. Vui lòng liên hệ Admin để cấu hình GEMINI_API_KEY.");
        }

        String prompt = promptBuilder.buildQuestionPrompt(subject, topic, difficulty, type, count);
        log.info("[GeminiAI] Sending prompt for {} questions: subject={}, topic={}, difficulty={}, type={}",
                count, subject, topic, difficulty, type);

        return callGeminiAndParse(prompt, subject, topic, difficulty, type);
    }

    /**
     * Sinh câu hỏi bù vào đề thi khi ngân hàng không đủ (KAN-23).
     *
     * <p>Prompt được tối ưu để tránh trùng lặp với các câu hỏi đã có trong đề.
     *
     * @param subject          Môn học
     * @param topic            Chủ đề
     * @param difficulty       Độ khó
     * @param type             Loại câu hỏi
     * @param gapCount         Số câu cần sinh bù
     * @param existingContents Nội dung các câu hỏi đã có (để tránh trùng)
     * @return Danh sách QuestionDTO mới (chưa lưu DB)
     * @throws AIServiceException nếu Gemini trả về lỗi hoặc JSON không hợp lệ
     */
    public List<QuestionDTO> generateExamGapQuestions(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int gapCount,
            List<String> existingContents) {

        if (gapCount <= 0) {
            return List.of();
        }

        if (geminiClient == null) {
            throw new AIServiceException("Hệ thống AI chưa được thiết lập. Vui lòng liên hệ Admin để cấu hình GEMINI_API_KEY.");
        }

        String prompt = promptBuilder.buildExamGenerationPrompt(
                subject, topic, difficulty, type, gapCount, existingContents);

        log.info("[GeminiAI][ExamGap] Generating {} gap questions: subject={}, topic={}, difficulty={}, type={}",
                gapCount, subject, topic, difficulty, type);

        return callGeminiAndParse(prompt, subject, topic, difficulty, type);
    }

    // =========================================================================
    // Lesson Plan Generation
    // =========================================================================

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

    // =========================================================================
    // Raw AI call (direct prompt)
    // =========================================================================

    /**
     * Gọi AI trực tiếp với một chuỗi prompt đã build hoàn chỉnh.
     *
     * @param prompt Nội dung prompt đầy đủ
     * @return Văn bản phản hồi từ AI
     */
    public String callAi(String prompt) {
        if (geminiClient == null) {
            throw new AIServiceException("Hệ thống AI chưa được thiết lập. Vui lòng liên hệ Admin để cấu hình GEMINI_API_KEY.");
        }

        try {
            GenerateContentResponse response = geminiClient.models.generateContent(model, prompt, null);
            return response.text();
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.error("[GeminiAI] Lỗi gọi AI: {}", errorMsg);
            if (errorMsg != null && (errorMsg.contains("API key not valid") || errorMsg.contains("400"))) {
                throw new AIServiceException("Lỗi xác thực: API Key không hợp lệ. Hãy đảm bảo bạn không nhập thừa dấu ngoặc kép hoặc khoảng trắng.");
            }
            throw new AIServiceException("Không thể kết nối với AI: " + errorMsg);
        }
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    /**
     * Gọi Gemini API với prompt và parse kết quả thành danh sách QuestionDTO.
     */
    private List<QuestionDTO> callGeminiAndParse(
            String prompt,
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type) {

        String rawResponse;
        try {
            GenerateContentResponse response = geminiClient.models.generateContent(model, prompt, null);
            rawResponse = response != null ? response.text() : null;
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            log.error("[GeminiAI] API call failed: {}", errorMsg, e);
            if (errorMsg != null && errorMsg.contains("API key not valid")) {
                throw new AIServiceException("API Key không hợp lệ. Vui lòng kiểm tra lại biến môi trường GEMINI_API_KEY.");
            }
            throw new AIServiceException("Gemini AI service is unavailable: " + errorMsg);
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

    /**
     * Loại bỏ markdown code fence (```json ... ```) nếu Gemini thêm vào.
     * Dùng regex để xử lý chính xác hơn.
     */
    private String cleanJsonResponse(String raw) {
        if (raw == null) return "[]";

        String cleaned = raw.trim();

        // Regex: tìm JSON array/object bên trong optional markdown code fences
        Pattern pattern = Pattern.compile("```(?:json)?\\s*([\\[\\{].*?[\\]\\}])\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Fallback: strip markers thủ công
        return cleaned.replaceAll("```json|```", "").trim();
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

    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
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

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultVal) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
