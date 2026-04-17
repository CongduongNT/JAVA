package com.planbookai.backend.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.planbookai.backend.exception.AIServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GeminiVisionClientAdapter implements GeminiVisionClient {

    private final Client geminiClient;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    public GeminiVisionClientAdapter(Optional<Client> geminiClient) {
        this.geminiClient = geminiClient.orElse(null);
    }

    @Override
    public String extractAnswerSheetJson(byte[] fileContent, String mimeType, String prompt) {
        if (geminiClient == null) {
            throw new AIServiceException(
                    "He thong AI chua duoc thiet lap. Vui long lien he Admin de cau hinh GEMINI_API_KEY.");
        }
        if (fileContent == null || fileContent.length == 0) {
            throw new IllegalArgumentException("fileContent is required");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType is required");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }

        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(fileContent, mimeType));

        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .build();

        GenerateContentResponse response;
        try {
            response = geminiClient.models.generateContent(model, content, config);
        } catch (Exception ex) {
            throw new AIServiceException("Gemini Vision service is unavailable: " + ex.getMessage(), ex);
        }

        String rawJson = response != null ? response.text() : null;
        if (rawJson == null || rawJson.isBlank()) {
            throw new AIServiceException("Gemini Vision returned empty OCR response");
        }

        return rawJson;
    }
}
