package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
