package com.planbookai.backend.service;

import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.dto.PromptTemplateRequest;
import com.planbookai.backend.model.entity.AiPromptTemplate;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AiPromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AiPromptTemplateService {

    private final AiPromptTemplateRepository repository;

    private static final Logger logger = Logger.getLogger(AiPromptTemplateService.class.getName());
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
    public List<PromptTemplateDTO> getTemplatesByPurpose(String purpose, boolean onlyApproved) {
        logger.info("AiPromptTemplateService: Lọc templates - Mục đích: '" + purpose + "', Chỉ duyệt: " + onlyApproved);
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> {
                    boolean statusMatch = !onlyApproved || "APPROVED".equalsIgnoreCase(t.getStatus());
                    if (!statusMatch) {
                        logger.fine("AiPromptTemplateService: Loại bỏ template '" + t.getTitle() + "' vì trạng thái '" + t.getStatus() + "' không phải APPROVED (onlyApproved=" + onlyApproved + ")");
                    }
                    boolean purposeMatch = purpose == null || purpose.isBlank() || 
                                         (t.getPurpose() != null && t.getPurpose().equalsIgnoreCase(purpose.trim()));
                    if (!purposeMatch) {
                        logger.fine("AiPromptTemplateService: Loại bỏ template '" + t.getTitle() + "' vì mục đích không khớp. Mong muốn: '" + purpose + "', Thực tế: '" + t.getPurpose() + "'");
                    }
                    return statusMatch && purposeMatch;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getApprovedTemplatesByPurpose(String purpose) {
        return getTemplatesByPurpose(purpose, true);
    }

    @Transactional
    public PromptTemplateDTO createTemplate(PromptTemplateRequest request, User user) {
        // Nếu là Admin hoặc Manager thì tự động duyệt luôn
        String initialStatus = (user.getRole() != null && 
                (user.getRole().getName() == Role.RoleName.ADMIN || user.getRole().getName() == Role.RoleName.MANAGER)) 
                ? "APPROVED" : "PENDING";

        AiPromptTemplate template = AiPromptTemplate.builder()
                .title(request.getTitle())
                .purpose(request.getPurpose())
                .promptText(request.getPromptText())
                .variables(request.getVariables())
                .status(initialStatus)
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
        if (fullPrompt == null || fullPrompt.isBlank()) {
            throw new RuntimeException("Nội dung mẫu Prompt trống");
        }
        
        // Thay thế các biến {{variable}} bằng giá trị thực tế
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                String value = entry.getValue() != null ? entry.getValue() : "";
                fullPrompt = fullPrompt.replace("{{" + entry.getKey() + "}}", value);
            }
        }

        // Ở đây bạn sẽ gọi đến GeminiAiService (cần triển khai) để lấy kết quả
        // Tạm thời trả về Prompt đã build để bạn kiểm tra logic
        return "AI Response dựa trên: " + fullPrompt;
    }
}