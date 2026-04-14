package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.AnswerSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerSheetRepository extends JpaRepository<AnswerSheet, Long> {
}
