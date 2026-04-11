package com.planbookai.backend.repository;
import com.planbookai.backend.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionsRepository extends JpaRepository<Question, Long> {
    java.util.List<Question> findByIsApproved(boolean isApproved);
    
}

