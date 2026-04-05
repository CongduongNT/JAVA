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
import java.util.Locale;
import java.util.Map;

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

    public List<QuestionDTO> generateQuestions(
            String subject,
            String topic,
            Question.Difficulty difficulty,
            Question.QuestionType type,
            int count) {
        Question.Difficulty requestedDifficulty = difficulty != null ? difficulty : Question.Difficulty.MEDIUM;
        Question.QuestionType requestedType = type != null ? type : Question.QuestionType.MULTIPLE_CHOICE;

        String prompt = promptBuilder.buildQuestionPrompt(subject, topic, requestedDifficulty, requestedType, count);
        log.info("[GeminiAI] Sending prompt for {} questions: subject={}, topic={}, difficulty={}, type={}",
                count, subject, topic, requestedDifficulty, requestedType);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GeminiAI] AI request rejected because GEMINI_API_KEY is not configured.");
            throw new AIServiceException("Gemini AI is not configured. Set GEMINI_API_KEY to enable AI endpoints.");
        }

        String rawResponse;
        try {
            Client geminiClient = geminiClientProvider.getObject();
            GenerateContentResponse response = geminiClient.models.generateContent(model, prompt, null);
            rawResponse = response.text();
        } catch (Exception exception) {
            log.error("[GeminiAI] API call failed: {}", exception.getMessage(), exception);
            throw new AIServiceException("Gemini AI service is unavailable: " + exception.getMessage());
        }

        log.debug("[GeminiAI] Raw response: {}", rawResponse);

        String cleanedJson = cleanJsonResponse(rawResponse);

        List<Map<String, Object>> rawQuestions;
        try {
            rawQuestions = objectMapper.readValue(cleanedJson, new TypeReference<>() {});
        } catch (Exception exception) {
            log.error("[GeminiAI] Failed to parse JSON response: {}", cleanedJson);
            throw new AIServiceException("AI returned invalid JSON. Please try again.");
        }

        return rawQuestions.stream()
                .map(raw -> mapRawToDTO(raw, subject, topic, requestedDifficulty, requestedType))
                .toList();
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) {
            return "[]";
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "[]";
        }
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
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
                Question.QuestionType.class, getStr(raw, "type", type.name()), type);
        Question.Difficulty parsedDifficulty = parseEnum(
                Question.Difficulty.class, getStr(raw, "difficulty", difficulty.name()), difficulty);

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
        Object value = map.get(key);
        return value != null ? value.toString() : defaultVal;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value, T defaultVal) {
        if (value == null || value.isBlank()) {
            return defaultVal;
        }
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return defaultVal;
        }
    }
}
