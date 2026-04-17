package com.planbookai.backend.service;

public interface GeminiVisionClient {

    String extractAnswerSheetJson(byte[] fileContent, String mimeType, String prompt);
}
