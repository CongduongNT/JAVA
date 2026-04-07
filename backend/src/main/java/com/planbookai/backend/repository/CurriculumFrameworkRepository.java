package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.CurriculumFramework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurriculumFrameworkRepository extends JpaRepository<CurriculumFramework, Integer> {

    Page<CurriculumFramework> findBySubjectAndGradeLevelAndIsPublishedTrue(String subject, String gradeLevel, Pageable pageable);

    Page<CurriculumFramework> findBySubjectAndIsPublishedTrue(String subject, Pageable pageable);

    Page<CurriculumFramework> findByGradeLevelAndIsPublishedTrue(String gradeLevel, Pageable pageable);

    Page<CurriculumFramework> findByIsPublishedTrue(Pageable pageable);

    Page<CurriculumFramework> findBySubjectAndGradeLevel(String subject, String gradeLevel, Pageable pageable);

    Page<CurriculumFramework> findBySubject(String subject, Pageable pageable);

    Page<CurriculumFramework> findByGradeLevel(String gradeLevel, Pageable pageable);

    List<CurriculumFramework> findByIsPublishedTrueOrderByCreatedAtDesc();
}
