package com.planbookai.backend.service;

import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.dto.PromptTemplateRequest;
import com.planbookai.backend.model.entity.AiPromptTemplate;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.Role.RoleName;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.AiPromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiPromptTemplateService {

    private final AiPromptTemplateRepository repository;
    private final GeminiAIService geminiAIService;

    private static final Logger log = LoggerFactory.getLogger(AiPromptTemplateService.class);

    public AiPromptTemplateService(AiPromptTemplateRepository repository, GeminiAIService geminiAIService) {
        this.repository = repository;
        this.geminiAIService = geminiAIService;
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getAllTemplates() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getTemplatesByPurpose(String purpose, boolean onlyApproved) {
        log.info("AiPromptTemplateService: Lọc templates - Mục đích: '{}', Chỉ duyệt: {}", purpose, onlyApproved);
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> {
                    boolean statusMatch = !onlyApproved || "APPROVED".equalsIgnoreCase(t.getStatus());
                    if (!statusMatch) {
                        log.debug("Loại bỏ template '{}' vì trạng thái '{}' không phải APPROVED", t.getTitle(), t.getStatus());
                    }
                    boolean purposeMatch = purpose == null || purpose.isBlank() || 
                                         (t.getPurpose() != null && t.getPurpose().trim().equalsIgnoreCase(purpose.trim()));
                    if (!purposeMatch) {
                        log.debug("Loại bỏ template '{}' vì mục đích không khớp. Mong muốn: '{}', Thực tế: '{}'", t.getTitle(), purpose, t.getPurpose());
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
        String initialStatus = (user != null && user.getRole() != null && 
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
    public PromptTemplateDTO approveTemplate(Long id, String status, User manager) {
        AiPromptTemplate template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        // Quy tắc nghiệp vụ: Chỉ có thể duyệt các bản mẫu đang chờ (PENDING)
        if (!"PENDING".equalsIgnoreCase(template.getStatus())) {
            throw new RuntimeException("Chỉ có thể thay đổi trạng thái cho các bản ghi đang ở trạng thái PENDING");
        }

        String targetStatus = (status != null) ? status.toUpperCase() : "APPROVED";
        
        if (!"APPROVED".equals(targetStatus) && !"REJECTED".equals(targetStatus)) {
            throw new RuntimeException("Trạng thái phê duyệt không hợp lệ. Chỉ chấp nhận APPROVED hoặc REJECTED");
        }

        template.setStatus(targetStatus);
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
        // Sử dụng Regex để thay thế chính xác các placeholder
        if (inputs != null) {
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue() != null ? entry.getValue() : "";
                // Sửa lỗi escape sequence: dùng \\s thay vì \s, \\{ thay vì \{
                String regex = "\\{\\{\\s*" + Pattern.quote(key) + "\\s*\\}\\}";
                fullPrompt = fullPrompt.replaceAll(regex, Matcher.quoteReplacement(value));
            }
        }
        
        // Xóa các placeholder còn sót lại chưa được điền giá trị (tránh làm AI bối rối)
        fullPrompt = fullPrompt.replaceAll("\\{\\{.*?\\}\\}", "");

        String result = geminiAIService.callAi(fullPrompt);
        if (result == null || result.isBlank()) throw new RuntimeException("AI không thể tạo nội dung từ mẫu này");
        return result;
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDTO> getTemplatesByFilter(RoleName filterRole, String purpose, String status) {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .filter(t -> {
                    boolean roleMatch = filterRole == null || (t.getCreatedBy() != null && 
                                       t.getCreatedBy().getRole() != null && 
                                       t.getCreatedBy().getRole().getName() == filterRole);
                    boolean purposeMatch = purpose == null || purpose.isBlank() || 
                                         (t.getPurpose() != null && t.getPurpose().equalsIgnoreCase(purpose));
                    boolean statusMatch = status == null || status.isBlank() || 
                                        (t.getStatus() != null && t.getStatus().equalsIgnoreCase(status));
                    return roleMatch && purposeMatch && statusMatch;
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}