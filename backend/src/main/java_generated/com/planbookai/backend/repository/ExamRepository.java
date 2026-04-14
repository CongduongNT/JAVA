package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository truy cập dữ liệu cho bảng exams.
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    /** Lấy đề thi của giáo viên theo ID, sắp xếp mới nhất trước. */
    Page<Exam> findByTeacherId(Long teacherId, Pageable pageable);

    /** Đếm số đề của teacher theo môn học. */
    long countByTeacherIdAndSubject(Long teacherId, String subject);

    /** Lọc đề theo teacher + subject + status. */
    @Query("""
            select e from Exam e
            where e.teacher.id = :teacherId
              and (:subject is null or e.subject = :subject)
              and (:status is null  or e.status  = :status)
            """)
    Page<Exam> findByTeacherWithFilters(
            @Param("teacherId") Long teacherId,
            @Param("subject")   String subject,
            @Param("status")    Exam.ExamStatus status,
            Pageable pageable);
}
