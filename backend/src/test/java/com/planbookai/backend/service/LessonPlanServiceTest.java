package com.planbookai.backend.service;

import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.model.entity.LessonPlan;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.LessonPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LessonPlanServiceTest {

    @Test
    void getMyLessonPlansReturnsPagedSummaryForTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        LessonPlanListItemDTO item = LessonPlanListItemDTO.builder()
                .id(101L)
                .frameworkId(3)
                .title("Atomic Structure")
                .subject("Chemistry")
                .gradeLevel("10")
                .topic("Atomic")
                .durationMinutes(45)
                .aiGenerated(true)
                .status(LessonPlan.LessonPlanStatus.DRAFT)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
        Page<LessonPlanListItemDTO> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        RepositoryInvocation invocation = new RepositoryInvocation();
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(page, invocation));

        PageResponse<LessonPlanListItemDTO> response = lessonPlanService.getMyLessonPlans(
                teacher,
                0,
                10,
                "draft",
                " Chemistry ",
                " 10 ",
                " Atomic ");

        assertEquals(teacher.getId(), invocation.teacherId);
        assertEquals(LessonPlan.LessonPlanStatus.DRAFT, invocation.status);
        assertEquals("Chemistry", invocation.subject);
        assertEquals("10", invocation.gradeLevel);
        assertEquals("Atomic", invocation.keyword);
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertFalse(response.isEmpty());
        assertSame(item, response.getContent().get(0));
        assertEquals(0, invocation.pageable.getPageNumber());
        assertEquals(10, invocation.pageable.getPageSize());
        assertEquals("DESC", invocation.pageable.getSort().getOrderFor("updatedAt").getDirection().name());
        assertEquals("DESC", invocation.pageable.getSort().getOrderFor("id").getDirection().name());
    }

    @Test
    void getMyLessonPlansRejectsNonTeacherUser() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(Page.empty(), new RepositoryInvocation()));
        User staff = buildUser(9L, Role.RoleName.STAFF);

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> lessonPlanService.getMyLessonPlans(staff, 0, 10, null, null, null, null));

        assertEquals("Only teacher can view lesson plans", exception.getMessage());
    }

    @Test
    void getMyLessonPlansRejectsInvalidStatus() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(Page.empty(), new RepositoryInvocation()));
        User teacher = buildUser(7L, Role.RoleName.TEACHER);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonPlanService.getMyLessonPlans(teacher, 0, 10, "ARCHIVED", null, null, null));

        assertTrue(exception.getMessage().contains("Invalid status: ARCHIVED"));
    }

    @Test
    void getMyLessonPlansRejectsOversizedPage() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(Page.empty(), new RepositoryInvocation()));
        User teacher = buildUser(7L, Role.RoleName.TEACHER);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonPlanService.getMyLessonPlans(teacher, 0, 101, null, null, null, null));

        assertEquals("size must not exceed 100", exception.getMessage());
    }

    private LessonPlanRepository createRepository(Page<LessonPlanListItemDTO> response, RepositoryInvocation invocation) {
        return (LessonPlanRepository) Proxy.newProxyInstance(
                LessonPlanRepository.class.getClassLoader(),
                new Class<?>[]{LessonPlanRepository.class},
                (proxy, method, args) -> {
                    if ("findByTeacherIdWithFilters".equals(method.getName())) {
                        invocation.teacherId = (Long) args[0];
                        invocation.status = (LessonPlan.LessonPlanStatus) args[1];
                        invocation.subject = (String) args[2];
                        invocation.gradeLevel = (String) args[3];
                        invocation.keyword = (String) args[4];
                        invocation.pageable = (Pageable) args[5];
                        return response;
                    }
                    if ("toString".equals(method.getName())) {
                        return "LessonPlanRepositoryProxy";
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private User buildUser(Long id, Role.RoleName roleName) {
        Role role = Role.builder().id(1).name(roleName).build();
        return User.builder()
                .id(id)
                .email("teacher@planbookai.com")
                .fullName("Teacher")
                .passwordHash("encoded")
                .role(role)
                .build();
    }

    private static final class RepositoryInvocation {
        private Long teacherId;
        private LessonPlan.LessonPlanStatus status;
        private String subject;
        private String gradeLevel;
        private String keyword;
        private Pageable pageable;
    }
}
