package com.planbookai.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planbookai.backend.dto.AnswerSheetDTO;
import com.planbookai.backend.exception.AIServiceException;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.mapper.AnswerSheetMapper;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AnswerSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OCRService {

    private static final Pattern JSON_FENCE_PATTERN =
            Pattern.compile("```(?:json)?\\s*([\\[\\{].*?[\\]\\}])\\s*```", Pattern.DOTALL);

    private final AnswerSheetRepository answerSheetRepository;
    private final AnswerSheetFileLoader answerSheetFileLoader;
    private final GeminiVisionClient geminiVisionClient;
    private final ObjectMapper objectMapper;
    private final AnswerSheetAccessGuard accessGuard;

    @Autowired
    public OCRService(
            AnswerSheetRepository answerSheetRepository,
            AnswerSheetFileLoader answerSheetFileLoader,
            GeminiVisionClient geminiVisionClient,
            ObjectMapper objectMapper,
            AnswerSheetAccessGuard accessGuard) {
        this.answerSheetRepository = answerSheetRepository;
        this.answerSheetFileLoader = answerSheetFileLoader;
        this.geminiVisionClient = geminiVisionClient;
        this.objectMapper = objectMapper;
        this.accessGuard = accessGuard;
    }

    public OCRService(
            AnswerSheetRepository answerSheetRepository,
            AnswerSheetFileLoader answerSheetFileLoader,
            GeminiVisionClient geminiVisionClient,
            ObjectMapper objectMapper) {
        this(
                answerSheetRepository,
                answerSheetFileLoader,
                geminiVisionClient,
                objectMapper,
                new AnswerSheetAccessGuard());
    }

    @Transactional
    public AnswerSheetDTO processAnswerSheet(Long answerSheetId, User user) {
        accessGuard.requireTeacher(user, "Only teacher can process answer sheets");

        AnswerSheet answerSheet = answerSheetRepository.findByIdForUpdate(answerSheetId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer sheet not found: " + answerSheetId));
        accessGuard.requireOwnedAnswerSheet(answerSheet, user, "You do not have permission to process this answer sheet");

        if (answerSheet.getOcrStatus() == AnswerSheet.OcrStatus.PROCESSING) {
            throw new IllegalArgumentException("Answer sheet is already being processed");
        }

        if (answerSheet.getOcrStatus() == AnswerSheet.OcrStatus.COMPLETED
                && hasText(answerSheet.getOcrRawData())) {
            return AnswerSheetMapper.toDTO(answerSheet);
        }

        answerSheet.setOcrStatus(AnswerSheet.OcrStatus.PROCESSING);
        answerSheetRepository.save(answerSheet);

        try {
            AnswerSheetFileLoader.LoadedAnswerSheetFile loadedFile =
                    answerSheetFileLoader.load(answerSheet.getFileUrl());
            String rawJson = geminiVisionClient.extractAnswerSheetJson(
                    loadedFile.getContent(),
                    loadedFile.getMimeType(),
                    buildOcrPrompt());

            applyOcrResult(answerSheet, rawJson);
            answerSheet.setOcrStatus(AnswerSheet.OcrStatus.COMPLETED);
            return AnswerSheetMapper.toDTO(answerSheetRepository.save(answerSheet));
        } catch (RuntimeException ex) {
            markFailed(answerSheet, ex.getMessage());
            if (ex instanceof AIServiceException) {
                throw ex;
            }
            throw new AIServiceException("OCR processing failed: " + ex.getMessage(), ex);
        }
    }

    private void applyOcrResult(AnswerSheet answerSheet, String rawJson) {
        Map<String, Object> parsed = parseOcrJson(rawJson);
        answerSheet.setOcrRawData(writeJson(parsed));

        String studentName = firstNonBlank(parsed, "student_name", "studentName");
        String studentCode = firstNonBlank(parsed, "student_code", "studentCode");

        answerSheet.setStudentName(studentName);
        answerSheet.setStudentCode(studentCode);
    }

    private Map<String, Object> parseOcrJson(String rawJson) {
        String cleanedJson = cleanJsonResponse(rawJson);

        try {
            Map<String, Object> parsed = objectMapper.readValue(
                    cleanedJson,
                    new TypeReference<Map<String, Object>>() {
                    });
            if (parsed == null || parsed.isEmpty()) {
                throw new AIServiceException("Gemini Vision returned empty OCR JSON");
            }
            return parsed;
        } catch (AIServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AIServiceException("Gemini Vision returned invalid OCR JSON", ex);
        }
    }

    private void markFailed(AnswerSheet answerSheet, String message) {
        answerSheet.setOcrStatus(AnswerSheet.OcrStatus.FAILED);
        answerSheet.setOcrRawData(writeJson(Map.of("error", message != null ? message : "OCR processing failed")));
        answerSheetRepository.save(answerSheet);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize OCR result", ex);
        }
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) {
            return "{}";
        }

        String cleaned = raw.trim();
        Matcher matcher = JSON_FENCE_PATTERN.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return cleaned.replaceAll("```json|```", "").trim();
    }

    private String buildOcrPrompt() {
        return """
                You are an OCR engine for Vietnamese student answer sheets.
                Read the uploaded answer sheet carefully and extract all legible content.

                Return strict JSON only with this shape:
                {
                  "student_name": "string or null",
                  "student_code": "string or null",
                  "extracted_text": "full recognized text",
                  "answers": [
                    {
                      "question_number": "string",
                      "answer": "string",
                      "evidence": "short excerpt from the sheet if available"
                    }
                  ],
                  "notes": [
                    "list of OCR uncertainties, empty if none"
                  ]
                }

                Rules:
                - Do not return markdown.
                - Preserve Vietnamese text exactly when possible.
                - If a field is missing, use null or empty array.
                - If handwriting is unclear, mention it in notes.
                """;
    }

    private String firstNonBlank(Map<String, Object> values, String... keys) {
        for (String key : keys) {
            Object value = values.get(key);
            if (value == null) {
                continue;
            }
            String text = value.toString().trim();
            if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
