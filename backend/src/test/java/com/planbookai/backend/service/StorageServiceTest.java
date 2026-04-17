package com.planbookai.backend.service;

import com.planbookai.backend.util.FileStorageUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class StorageServiceTest {

    @Test
    void uploadMultipartFileDelegatesToFileStorageUtil() {
        RecordingFileStorageUtil fileStorageUtil = new RecordingFileStorageUtil();
        StorageService storageService = new StorageService(fileStorageUtil);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "answer.png",
                "image/png",
                "content".getBytes(StandardCharsets.UTF_8));

        String url = storageService.uploadFile("answer-sheets", file);

        assertEquals("answer-sheets", fileStorageUtil.folder);
        assertSame(file, fileStorageUtil.multipartFile);
        assertEquals("https://demo.supabase.co/mock-upload", url);
    }

    @Test
    void uploadInputStreamDelegatesToFileStorageUtil() {
        RecordingFileStorageUtil fileStorageUtil = new RecordingFileStorageUtil();
        StorageService storageService = new StorageService(fileStorageUtil);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("pdf".getBytes(StandardCharsets.UTF_8));

        String url = storageService.uploadFile("answer-sheets", "sheet.pdf", inputStream, 3);

        assertEquals("answer-sheets", fileStorageUtil.folder);
        assertEquals("sheet.pdf", fileStorageUtil.originalFilename);
        assertSame(inputStream, fileStorageUtil.inputStream);
        assertEquals(3, fileStorageUtil.contentLength);
        assertEquals("https://demo.supabase.co/mock-upload", url);
    }

    private static class RecordingFileStorageUtil extends FileStorageUtil {

        private String folder;
        private MultipartFile multipartFile;
        private String originalFilename;
        private InputStream inputStream;
        private long contentLength;

        private RecordingFileStorageUtil() {
            super(createNoOpS3Client(), "planbookai-storage", "https://demo.supabase.co");
        }

        @Override
        public String uploadFile(String folder, MultipartFile file) {
            this.folder = folder;
            this.multipartFile = file;
            return "https://demo.supabase.co/mock-upload";
        }

        @Override
        public String uploadFile(String folder, String originalFilename, InputStream inputStream, long contentLength) {
            this.folder = folder;
            this.originalFilename = originalFilename;
            this.inputStream = inputStream;
            this.contentLength = contentLength;
            return "https://demo.supabase.co/mock-upload";
        }

        private static S3Client createNoOpS3Client() {
            return (S3Client) Proxy.newProxyInstance(
                    S3Client.class.getClassLoader(),
                    new Class<?>[]{S3Client.class},
                    (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "serviceName":
                                return "s3";
                            case "close":
                                return null;
                            case "toString":
                                return "S3ClientNoOpProxy";
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "equals":
                                return proxy == args[0];
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    });
        }
    }
}
