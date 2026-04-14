package com.planbookai.backend.controller;

import com.planbookai.backend.dto.ErrorResponse;
import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.dto.PromptTemplateRequest;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AiPromptTemplateService;
import com.planbookai.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prompt-templates")
public class PromptTemplateController {

    private final AiPromptTemplateService aiPromptTemplateService;
    private final AuthService authService;

    public PromptTemplateController(AiPromptTemplateService aiPromptTemplateService, AuthService authService) {
        this.aiPromptTemplateService = aiPromptTemplateService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<PromptTemplateDTO>> getTemplates(
            @RequestParam(required = false) String purpose,
            Authentication authentication) {
        
        // Kiểm tra quyền hạn dựa trên Authorities của SecurityContext
        boolean canSeeAll = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || 
                                 role.equals("ROLE_MANAGER") || 
                                 role.equals("ROLE_STAFF"));

        List<PromptTemplateDTO> templates = canSeeAll 
                ? aiPromptTemplateService.getTemplatesByPurpose(purpose, false) 
                : aiPromptTemplateService.getTemplatesByPurpose(purpose, true);
                
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/approved")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<PromptTemplateDTO>> getApprovedTemplates(@RequestParam(required = false) String purpose) {
        return ResponseEntity.ok(aiPromptTemplateService.getApprovedTemplatesByPurpose(purpose));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<PromptTemplateDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(aiPromptTemplateService.getAllTemplates().stream()
                .filter(t -> t.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RuntimeException("Template not found")));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<PromptTemplateDTO> create(@RequestBody PromptTemplateRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aiPromptTemplateService.createTemplate(request, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<PromptTemplateDTO> update(@PathVariable Long id, @RequestBody PromptTemplateRequest request) {
        return ResponseEntity.ok(aiPromptTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        aiPromptTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> approve(
            @PathVariable Long id, 
            @RequestBody(required = false) Map<String, String> body, 
            @AuthenticationPrincipal User manager) {
        try {
            String status = (body != null && body.containsKey("status")) ? body.get("status") : "APPROVED";
            aiPromptTemplateService.approveTemplate(id, status, manager);
            return ResponseEntity.ok().body("Template approved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('TEACHER', 'STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> payload) {
        try {
            if (!payload.containsKey("templateId") || payload.get("templateId") == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Thiếu ID mẫu (templateId)"));
            }
            
            Long templateId = Long.valueOf(payload.get("templateId").toString());
            @SuppressWarnings("unchecked")
            Map<String, String> inputs = (Map<String, String>) payload.get("inputs");
            
            String content = aiPromptTemplateService.generateContent(templateId, inputs);
            return ResponseEntity.ok(Map.of("content", content));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ErrorResponse("Lỗi xử lý AI: " + e.getMessage()));
        }
    }
}