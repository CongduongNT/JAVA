package com.planbookai.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${supabase.bucket:planbookai-storage}")
    private String bucketName;

    @Value("${supabase.url:your-url}")
    private String supabaseUrl;

    public String uploadFile(String folder, String originalFilename, InputStream inputStream, long contentLength) {
        String key = folder + "/" + UUID.randomUUID().toString() + "_" + originalFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

        // Returns public URL if bucket is public, otherwise needs signed URL
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, key);
    }
}
