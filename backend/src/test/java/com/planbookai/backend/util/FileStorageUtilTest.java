package com.planbookai.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageUtilTest {

    @Test
    void uploadMultipartFileStoresObjectAndReturnsPublicUrl() {
        UploadState state = new UploadState();
        FileStorageUtil fileStorageUtil = new FileStorageUtil(
                createS3Client(state),
                "planbookai-storage",
                "https://demo.supabase.co/");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                " Student A (final).PNG ",
                "image/png",
                "image-content".getBytes(StandardCharsets.UTF_8));

        String publicUrl = fileStorageUtil.uploadFile("answer sheets", file);

        assertEquals(1, state.putObjectCalls);
        assertNotNull(state.putObjectRequest);
        assertEquals("planbookai-storage", state.putObjectRequest.bucket());
        assertEquals("image/png", state.putObjectRequest.contentType());
        assertTrue(state.putObjectRequest.key().startsWith("answer-sheets/"));
        assertTrue(state.putObjectRequest.key().matches(
                "answer-sheets/[0-9a-f\\-]{36}_Student-A-final\\.png"));
        assertEquals(
                "https://demo.supabase.co/storage/v1/object/public/planbookai-storage/" + state.putObjectRequest.key(),
                publicUrl);
    }

    @Test
    void uploadInputStreamUsesDefaultContentTypeWhenMissing() {
        UploadState state = new UploadState();
        FileStorageUtil fileStorageUtil = new FileStorageUtil(
                createS3Client(state),
                "planbookai-storage",
                "https://demo.supabase.co");
        byte[] content = "pdf".getBytes(StandardCharsets.UTF_8);

        String publicUrl = fileStorageUtil.uploadFile(
                "answer/sheets",
                "sheet 01.pdf",
                new ByteArrayInputStream(content),
                content.length,
                null);

        assertEquals(1, state.putObjectCalls);
        assertEquals("application/octet-stream", state.putObjectRequest.contentType());
        assertTrue(state.putObjectRequest.key().startsWith("answer/sheets/"));
        assertTrue(state.putObjectRequest.key().endsWith("_sheet-01.pdf"));
        assertEquals(
                "https://demo.supabase.co/storage/v1/object/public/planbookai-storage/" + state.putObjectRequest.key(),
                publicUrl);
    }

    @Test
    void uploadRejectsEmptyMultipartFile() {
        FileStorageUtil fileStorageUtil = new FileStorageUtil(
                createS3Client(new UploadState()),
                "planbookai-storage",
                "https://demo.supabase.co");

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageUtil.uploadFile("answer-sheets", emptyFile));

        assertEquals("file must not be empty", exception.getMessage());
    }

    @Test
    void uploadRejectsInvalidContentLength() {
        FileStorageUtil fileStorageUtil = new FileStorageUtil(
                createS3Client(new UploadState()),
                "planbookai-storage",
                "https://demo.supabase.co");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileStorageUtil.uploadFile("answer-sheets", "sheet.pdf", new ByteArrayInputStream(new byte[0]), 0));

        assertEquals("contentLength must be greater than 0", exception.getMessage());
    }

    private S3Client createS3Client(UploadState state) {
        return (S3Client) Proxy.newProxyInstance(
                S3Client.class.getClassLoader(),
                new Class<?>[]{S3Client.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "putObject":
                            state.putObjectCalls++;
                            state.putObjectRequest = (PutObjectRequest) args[0];
                            state.requestBody = (RequestBody) args[1];
                            return PutObjectResponse.builder().eTag("etag").build();
                        case "serviceName":
                            return "s3";
                        case "close":
                            return null;
                        case "toString":
                            return "S3ClientProxy";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
                });
    }

    private static class UploadState {
        private PutObjectRequest putObjectRequest;
        private RequestBody requestBody;
        private int putObjectCalls;
    }
}
