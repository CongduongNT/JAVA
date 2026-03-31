package components.guards;

import com.planbookai.backend.model.entity.PromptTemplate;
import com.planbookai.backend.repository.PromptTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromptTemplateService {

    private final PromptTemplateRepository promptTemplateRepository;

    public PromptTemplateService(PromptTemplateRepository promptTemplateRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
    }

    @Transactional
    public void approveTemplate(Long id) {
        PromptTemplate template = promptTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prompt Template không tồn tại với ID: " + id));
        
        template.setStatus("APPROVED");
        promptTemplateRepository.save(template);
    }
}
