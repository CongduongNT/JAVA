package com.planbookai.backend.service;

import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.dto.PromptTemplateRequest;
import com.planbookai.backend.model.entity.AiPromptTemplate;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AiPromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiPromptTemplateService {

    private final AiPromptTemplateRepository repository;

    public AiPromptTemplateService(AiPromptTemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getAllTemplates() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getApprovedTemplatesByPurpose(String purpose) {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> "APPROVED".equals(t.getStatus()))
                .filter(t -> purpose == null || purpose.equalsIgnoreCase(t.getPurpose()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PromptTemplateDTO createTemplate(PromptTemplateRequest request, User user) {
        AiPromptTemplate template = AiPromptTemplate.builder()
                .title(request.getTitle())
                .purpose(request.getPurpose())
                .promptText(request.getPromptText())
                .variables(request.getVariables())
                .status("PENDING")
                .createdBy(user)
                .build();
        
        return mapToDTO(repository.save(template));
    }

    @Transactional
    public PromptTemplateDTO updateTemplate(Long id, PromptTemplateRequest request) {
        AiPromptTemplate existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        existing.setTitle(request.getTitle());
        existing.setPurpose(request.getPurpose());
        existing.setPromptText(request.getPromptText());
        existing.setVariables(request.getVariables());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return mapToDTO(repository.save(existing));
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Template not found");
        }
        repository.deleteById(id);
    }

    private PromptTemplateDTO mapToDTO(AiPromptTemplate entity) {
        return PromptTemplateDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .purpose(entity.getPurpose())
                .promptText(entity.getPromptText())
                .variables(entity.getVariables())
                .status(entity.getStatus())
                .createdByName(entity.getCreatedBy() != null ? entity.getCreatedBy().getFullName() : "Unknown")
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Transactional
    public PromptTemplateDTO approveTemplate(Long id, User manager) {
        AiPromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setStatus("APPROVED");
        template.setUpdatedAt(LocalDateTime.now());
        
        return mapToDTO(repository.save(template));
    }

    @Transactional(readOnly = true)
    public String generateContent(Long templateId, Map<String, String> inputs) {
        AiPromptTemplate template = repository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template không tồn tại"));

        if (!"APPROVED".equals(template.getStatus())) {
            throw new RuntimeException("Template chưa được phê duyệt để sử dụng");
        }

        String fullPrompt = template.getPromptText();
        
        // Thay thế các biến {{variable}} bằng giá trị thực tế
        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            fullPrompt = fullPrompt.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        // Ở đây bạn sẽ gọi đến GeminiAiService (cần triển khai) để lấy kết quả
        // Tạm thời trả về Prompt đã build để bạn kiểm tra logic
        return "AI Response dựa trên: " + fullPrompt;
    }
}