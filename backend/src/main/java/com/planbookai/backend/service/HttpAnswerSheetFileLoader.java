package com.planbookai.backend.service;

import com.planbookai.backend.exception.AIServiceException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpAnswerSheetFileLoader implements AnswerSheetFileLoader {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    private final HttpClient httpClient;

    public HttpAnswerSheetFileLoader() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(15))
                .build());
    }

    HttpAnswerSheetFileLoader(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public LoadedAnswerSheetFile load(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("answer sheet fileUrl is required");
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(fileUrl.trim()))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<byte[]> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AIServiceException("Interrupted while downloading answer sheet file", ex);
        } catch (Exception ex) {
            throw new AIServiceException("Failed to download answer sheet file: " + ex.getMessage(), ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new AIServiceException("Failed to download answer sheet file: HTTP " + response.statusCode());
        }

        byte[] content = response.body();
        if (content == null || content.length == 0) {
            throw new AIServiceException("Downloaded answer sheet file is empty");
        }

        String mimeType = response.headers()
                .firstValue("Content-Type")
                .map(this::normalizeMimeType)
                .orElseGet(() -> guessMimeType(fileUrl));

        return new LoadedAnswerSheetFile(content, mimeType);
    }

    private String normalizeMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_MIME_TYPE;
        }

        int separatorIndex = contentType.indexOf(';');
        String normalized = separatorIndex >= 0 ? contentType.substring(0, separatorIndex) : contentType;
        normalized = normalized.trim();
        return normalized.isEmpty() ? DEFAULT_MIME_TYPE : normalized;
    }

    private String guessMimeType(String fileUrl) {
        String guessed = URLConnection.guessContentTypeFromName(fileUrl);
        return guessed != null && !guessed.isBlank() ? guessed : DEFAULT_MIME_TYPE;
    }
}
