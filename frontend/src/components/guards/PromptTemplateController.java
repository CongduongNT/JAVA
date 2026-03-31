package components.guards;

import com.planbookai.backend.dto.ErrorResponse;
import com.planbookai.backend.dto.PromptTemplateDTO;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.service.AiPromptTemplateService;
import com.planbookai.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<PromptTemplateDTO>> getTemplates(@RequestParam(required = false) String purpose) {
        // Nếu là Teacher, chỉ lấy những cái đã APPROVED
        List<PromptTemplateDTO> templates = aiPromptTemplateService.getApprovedTemplatesByPurpose(purpose);
        return ResponseEntity.ok(templates);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        try {
            User manager = authService.getCurrentUser();
            aiPromptTemplateService.approveTemplate(id, manager);
            return ResponseEntity.ok().body("Template approved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}