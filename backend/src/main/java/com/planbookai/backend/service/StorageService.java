package com.planbookai.backend.service;

import com.planbookai.backend.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final FileStorageUtil fileStorageUtil;

    public String uploadFile(String folder, MultipartFile file) {
        return fileStorageUtil.uploadFile(folder, file);
    }

    public String uploadFile(String folder, String originalFilename, InputStream inputStream, long contentLength) {
        return fileStorageUtil.uploadFile(folder, originalFilename, inputStream, contentLength);
    }
}
