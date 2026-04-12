package com.planbookai.backend.repository;

import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.model.entity.LessonPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonPlanRepository extends JpaRepository<LessonPlan, Long> {

    @Query("""
            select new com.planbookai.backend.dto.LessonPlanListItemDTO(
                lp.id,
                lp.frameworkId,
                lp.title,
                lp.subject,
                lp.gradeLevel,
                lp.topic,
                lp.durationMinutes,
                lp.aiGenerated,
                lp.status,
                lp.createdAt,
                lp.updatedAt
            )
            from LessonPlan lp
            where lp.teacher.id = :teacherId
              and (:status is null or lp.status = :status)
              and (:subject is null or lower(coalesce(lp.subject, '')) like lower(concat('%', :subject, '%')))
              and (:gradeLevel is null or lower(coalesce(lp.gradeLevel, '')) like lower(concat('%', :gradeLevel, '%')))
              and (
                    :keyword is null
                    or lower(coalesce(lp.title, '')) like lower(concat('%', :keyword, '%'))
                    or lower(coalesce(lp.topic, '')) like lower(concat('%', :keyword, '%'))
                  )
            """)
    Page<LessonPlanListItemDTO> findByTeacherIdWithFilters(
            @Param("teacherId") Long teacherId,
            @Param("status") LessonPlan.LessonPlanStatus status,
            @Param("subject") String subject,
            @Param("gradeLevel") String gradeLevel,
            @Param("keyword") String keyword,
            Pageable pageable);

    List<LessonPlan> findByTeacher_IdOrderByUpdatedAtDesc(Long teacherId);
}
