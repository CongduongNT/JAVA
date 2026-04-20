package com.planbookai.backend.service;

import com.planbookai.backend.dto.SystemConfigDTO;
import com.planbookai.backend.dto.UpdateSystemConfigRequest;
import com.planbookai.backend.exception.ResourceNotFoundException;
import com.planbookai.backend.model.entity.SystemConfig;
import com.planbookai.backend.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository repository;

    public List<SystemConfigDTO> getAll() {
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public SystemConfigDTO getByKey(String key) {
        return toDTO(findOrThrow(key));
    }

    public SystemConfigDTO update(String key, UpdateSystemConfigRequest req, String adminEmail) {
        SystemConfig config = findOrThrow(key);
        config.setConfigValue(req.getConfigValue());
        config.setUpdatedBy(adminEmail);
        return toDTO(repository.save(config));
    }

    private SystemConfig findOrThrow(String key) {
        return repository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("System config not found: " + key));
    }

    private SystemConfigDTO toDTO(SystemConfig c) {
        return SystemConfigDTO.builder()
                .configKey(c.getConfigKey())
                .configValue(c.getConfigValue())
                .description(c.getDescription())
                .updatedBy(c.getUpdatedBy())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
