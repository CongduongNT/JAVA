package com.planbookai.backend.repository;

import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository truy cập dữ liệu cho bảng users.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // ── User analytics queries (KAN-26) ───────────────────────────────────────

    /** Tất cả users sắp xếp mới nhất trước. */
    @Query("select u from User u order by u.createdAt desc")
    List<User> findAllOrderByCreatedAtDesc();

    /** Số user theo role name. */
    @Query("select count(u) from User u where u.role.name = :roleName")
    long countByRoleName(@Param("roleName") Role.RoleName roleName);

    /** Số user đăng ký trong khoảng thời gian. */
    @Query("select count(u) from User u where u.createdAt between :from and :to")
    long countByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** Users đăng ký trong khoảng thời gian, sắp xếp mới nhất trước. */
    @Query("select u from User u where u.createdAt between :from and :to order by u.createdAt desc")
    List<User> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to")   LocalDateTime to);

    /** Tất cả users có role TEACHER. */
    @Query("select u from User u where u.role.name = 'TEACHER' order by u.createdAt desc")
    List<User> findAllTeachers();
}