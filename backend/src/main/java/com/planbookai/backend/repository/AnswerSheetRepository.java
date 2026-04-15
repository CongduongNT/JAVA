package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.AnswerSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface AnswerSheetRepository extends JpaRepository<AnswerSheet, Long> {

    Page<AnswerSheet> findByTeacher_Id(Long teacherId, Pageable pageable);

    Page<AnswerSheet> findByTeacher_IdAndExam_Id(Long teacherId, Long examId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select answerSheet from AnswerSheet answerSheet where answerSheet.id = :id")
    Optional<AnswerSheet> findByIdForUpdate(@Param("id") Long id);
}
