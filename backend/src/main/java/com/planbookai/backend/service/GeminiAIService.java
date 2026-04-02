package com.planbookai.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.planbookai.backend.dto.QuestionDTO;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.model.entity.Question;
import com.planbookai.backend.util.PromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GeminiAIService – Gọi Gemini API, parse JSON response → danh sách QuestionDTO.
 *
 * <p>Luồng xử lý:
 * <ol>
 *   <li>PromptBuilder xây dựng prompt theo tham số.</li>
 *   <li>Gửi prompt lên Gemini (model: gemini-2.0-flash).</li>
 *   <li>Trích xuất text response và loại bỏ markdown code fence nếu có.</li>
 *   <li>Parse JSON array → List&lt;QuestionDTO&gt; (chưa có id, chưa lưu DB).</li>
 * </ol>
 *
 * <p>Nếu Gemini trả về dữ liệu không hợp lệ, ném {@link AIServiceException}.
 */
@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    private final ObjectProvider<Client> geminiClientProvider;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiAIService(ObjectProvider<Client> geminiClientProvider, PromptBuilder promptBuilder) {
        this.geminiClientProvider = geminiClientProvider;
        this.promptBuilder = promptBuilder;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sinh danh sách câu hỏi thông qua Gemini AI (chưa lưu DB).
     *
     * @param subject    Môn học
     * @param topic      Chủ đề
     * @param difficulty Độ khó
     * @param type       Loại câu hỏi
     * @param count      Số câu cần sinh
     * @return Danh sách QuestionDTO chưa có id (preview)
     * @throws AIServiceException nếu Gemini trả về lỗi hoặc JSON không hợp lệ
     */
    public List<QuestionDTO> generateQuestions(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int count) {

        // 1. Build prompt
        String prompt = promptBuilder.buildQuestionPrompt(subject, topic, difficulty, type, count);
        log.info("[GeminiAI] Sending prompt for {} questions: subject={}, topic={}, difficulty={}, type={}",
                count, subject, topic, difficulty, type);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GeminiAI] AI request rejected because GEMINI_API_KEY is not configured.");
            throw new AIServiceException("Gemini AI is not configured. Set GEMINI_API_KEY to enable AI endpoints.");
        }

        // 2. Call Gemini API
        String rawResponse;
        try {
            Client geminiClient = geminiClientProvider.getObject();
            GenerateContentResponse response = geminiClient.models.generateContent(
                    model, prompt, null);
            rawResponse = response.text();
        } catch (Exception e) {
            log.error("[GeminiAI] API call failed: {}", e.getMessage(), e);
            throw new AIServiceException("Gemini AI service is unavailable: " + e.getMessage());
        }

        log.debug("[GeminiAI] Raw response: {}", rawResponse);

        // 3. Clean response – strip markdown code fence if present
        String cleanedJson = cleanJsonResponse(rawResponse);

        // 4. Parse JSON array → list of raw maps
        List<Map<String, Object>> rawQuestions;
        try {
            rawQuestions = objectMapper.readValue(cleanedJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("[GeminiAI] Failed to parse JSON response: {}", cleanedJson);
            throw new AIServiceException("AI returned invalid JSON. Please try again.");
        }

        // 5. Convert raw maps → QuestionDTO
        return rawQuestions.stream()
                .map(raw -> mapRawToDTO(raw, subject, topic, difficulty, type))
                .toList();
    }

    /**
     * Loại bỏ markdown code fence (```json ... ```) nếu Gemini thêm vào.
     */
    private String cleanJsonResponse(String raw) {
        if (raw == null) return "[]";
        String trimmed = raw.trim();
        // Remove ```json or ``` at start
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
        }
        // Remove ``` at end
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```")).trim();
        }
        return trimmed;
    }

    /**
     * Map một raw JSON object → QuestionDTO.
     * Fallback về tham số truyền vào nếu Gemini thiếu field.
     */
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

        // Parse type & difficulty safely
        Question.QuestionType parsedType = parseEnum(
                Question.QuestionType.class, getStr(raw, "type", type.name()), type);
        Question.Difficulty parsedDifficulty = parseEnum(
                Question.Difficulty.class, getStr(raw, "difficulty", difficulty.name()), difficulty);

        // Parse options (only for MULTIPLE_CHOICE)
        List<Map<String, Object>> options = null;
        Object rawOptions = raw.get("options");
        if (rawOptions instanceof List<?> list && !list.isEmpty()) {
            options = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    options.add((Map<String, Object>) map);
                }
            }
        }

        QuestionDTO dto = new QuestionDTO();
        dto.setContent(content);
        dto.setType(parsedType.name());
        dto.setDifficulty(parsedDifficulty.name());
        dto.setTopic(topicVal);
        dto.setOptions(options);
        dto.setCorrectAnswer(correctAnswer);
        dto.setExplanation(explanation);
        dto.setAiGenerated(true);
        dto.setIsApproved(false);
        return dto;
    }

    private String getStr(Map<String, Object> map, String key, String defaultVal) {
        Object val = map.get(key);
        return val != null ? val.toString() : defaultVal;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultVal) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
