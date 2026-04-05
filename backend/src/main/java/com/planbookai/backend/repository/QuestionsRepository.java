package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionsRepository extends JpaRepository<Questions, Long> {
    java.util.List<Questions> findByIsApproved(boolean isApproved);
    
}
