package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.AiPromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, Long> {
    List<AiPromptTemplate> findAllByOrderByCreatedAtDesc();
    List<AiPromptTemplate> findByStatusOrderByCreatedAtDesc(String status);
}