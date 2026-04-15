package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.AnswerSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerSheetRepository extends JpaRepository<AnswerSheet, Long> {

    Page<AnswerSheet> findByTeacher_Id(Long teacherId, Pageable pageable);

    Page<AnswerSheet> findByTeacher_IdAndExam_Id(Long teacherId, Long examId, Pageable pageable);
}
