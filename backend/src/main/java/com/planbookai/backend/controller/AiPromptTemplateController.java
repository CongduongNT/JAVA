package com.planbookai.backend.controller;

import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.dto.PromptTemplateRequest;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AiPromptTemplateService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prompt-templates")
public class AiPromptTemplateController {

    private final AiPromptTemplateService service;

    public AiPromptTemplateController(AiPromptTemplateService service) {
        this.service = service;
    }

    // Dành cho Staff: Lấy tất cả
    @GetMapping
    public List<PromptTemplateDTO> getAll() {
        return service.getAllTemplates();
    }

    // Dành cho Teacher: Lấy các mẫu đã duyệt theo mục đích
    @GetMapping("/approved")
    public List<PromptTemplateDTO> getApproved(@RequestParam(required = false) String purpose) {
        return service.getApprovedTemplatesByPurpose(purpose);
    }

    @PostMapping
    public PromptTemplateDTO create(@RequestBody PromptTemplateRequest request, @AuthenticationPrincipal User user) {
        return service.createTemplate(request, user);
    }

    @PutMapping("/{id}")
    public PromptTemplateDTO update(@PathVariable Long id, @RequestBody PromptTemplateRequest request) {
        return service.updateTemplate(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteTemplate(id);
    }

    @PostMapping("/generate")
    public Map<String, String> generate(@RequestBody Map<String, Object> payload) {
        Long templateId = Long.valueOf(payload.get("templateId").toString());
        Map<String, String> inputs = (Map<String, String>) payload.get("inputs");
        String content = service.generateContent(templateId, inputs);
        return Map.of("content", content);
    }
}