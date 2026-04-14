package com.planbookai.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FileStorageUtil {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Client s3Client;
    private final String bucketName;
    private final String supabaseUrl;

    public FileStorageUtil(
            S3Client s3Client,
            @Value("${supabase.bucket:planbookai-storage}") String bucketName,
            @Value("${supabase.url:}") String supabaseUrl) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.supabaseUrl = supabaseUrl;
    }

    public String uploadFile(String folder, MultipartFile file) {
        validateMultipartFile(file);
        try (InputStream inputStream = file.getInputStream()) {
            return uploadFile(
                    folder,
                    file.getOriginalFilename(),
                    inputStream,
                    file.getSize(),
                    file.getContentType());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to read file content for upload", ex);
        }
    }

    public String uploadFile(String folder, String originalFilename, InputStream inputStream, long contentLength) {
        return uploadFile(folder, originalFilename, inputStream, contentLength, null);
    }

    public String uploadFile(
            String folder,
            String originalFilename,
            InputStream inputStream,
            long contentLength,
            String contentType) {
        String normalizedFolder = normalizeFolder(folder);
        String normalizedFilename = normalizeFilename(originalFilename);
        validateStreamInput(inputStream, contentLength);

        String key = normalizedFolder + "/" + UUID.randomUUID() + "_" + normalizedFilename;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(resolveContentType(contentType))
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        } catch (RuntimeException ex) {
            throw new RuntimeException("Failed to upload file to Supabase Storage", ex);
        }

        return buildPublicUrl(key);
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file is required");
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file must not be empty");
        }
    }

    private void validateStreamInput(InputStream inputStream, long contentLength) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is required");
        }
        if (contentLength <= 0) {
            throw new IllegalArgumentException("contentLength must be greater than 0");
        }
    }

    private String normalizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            throw new IllegalArgumentException("folder is required");
        }

        String normalized = folder.trim().replace('\\', '/');
        normalized = normalized.replaceAll("^/+", "").replaceAll("/+$", "");

        String sanitized = Arrays.stream(normalized.split("/"))
                .map(this::sanitizeSegment)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("/"));

        if (!StringUtils.hasText(sanitized)) {
            throw new IllegalArgumentException("folder must contain at least one valid path segment");
        }

        return sanitized;
    }

    private String normalizeFilename(String originalFilename) {
        String safeFilename = originalFilename != null ? originalFilename.trim().replace('\\', '/') : "";
        int lastSeparatorIndex = safeFilename.lastIndexOf('/');
        if (lastSeparatorIndex >= 0) {
            safeFilename = safeFilename.substring(lastSeparatorIndex + 1);
        }

        if (!StringUtils.hasText(safeFilename)) {
            throw new IllegalArgumentException("originalFilename is required");
        }

        String trimmedFilename = safeFilename.trim();
        int extensionIndex = trimmedFilename.lastIndexOf('.');
        String namePart = extensionIndex > 0 ? trimmedFilename.substring(0, extensionIndex) : trimmedFilename;
        String extensionPart = extensionIndex > 0 ? trimmedFilename.substring(extensionIndex).toLowerCase(Locale.ROOT) : "";

        String sanitizedName = sanitizeSegment(namePart);
        if (!StringUtils.hasText(sanitizedName)) {
            sanitizedName = "file";
        }

        String sanitizedExtension = extensionPart.replaceAll("[^a-z0-9.]", "");
        return sanitizedName + sanitizedExtension;
    }

    private String sanitizeSegment(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String sanitized = value.trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^A-Za-z0-9._-]", "-")
                .replaceAll("-{2,}", "-");

        return sanitized.replaceAll("^[._-]+", "").replaceAll("[._-]+$", "");
    }

    private String resolveContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType.trim() : DEFAULT_CONTENT_TYPE;
    }

    private String buildPublicUrl(String key) {
        String normalizedSupabaseUrl = supabaseUrl != null ? supabaseUrl.replaceAll("/+$", "") : "";
        return normalizedSupabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + key;
    }
}
