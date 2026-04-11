package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.LessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonPlanRepository extends JpaRepository<LessonPlan, Long> {

    List<LessonPlan> findByCreatedById(Long userId);

    List<LessonPlan> findByIsApproved(Boolean isApproved);

    List<LessonPlan> findBySubjectAndGradeLevel(String subject, String gradeLevel);
}
