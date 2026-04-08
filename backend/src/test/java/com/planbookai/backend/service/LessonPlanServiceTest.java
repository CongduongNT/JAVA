package com.planbookai.backend.service;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.LessonPlanRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.exception.ResourceNotFoundException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        RepositoryState state = new RepositoryState();
        state.listResponse = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        PageResponse<LessonPlanListItemDTO> response = lessonPlanService.getMyLessonPlans(
                teacher,
                0,
                10,
                "draft",
                " Chemistry ",
                " 10 ",
                " Atomic ");

        assertEquals(teacher.getId(), state.teacherId);
        assertEquals(LessonPlan.LessonPlanStatus.DRAFT, state.status);
        assertEquals("Chemistry", state.subject);
        assertEquals("10", state.gradeLevel);
        assertEquals("Atomic", state.keyword);
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        assertFalse(response.isEmpty());
        assertSame(item, response.getContent().get(0));
        assertEquals(0, state.pageable.getPageNumber());
        assertEquals(10, state.pageable.getPageSize());
        assertEquals("DESC", state.pageable.getSort().getOrderFor("updatedAt").getDirection().name());
        assertEquals("DESC", state.pageable.getSort().getOrderFor("id").getDirection().name());
    }

    @Test
    void getMyLessonPlansRejectsNonTeacherUser() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(new RepositoryState()));
        User staff = buildUser(9L, Role.RoleName.STAFF);

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> lessonPlanService.getMyLessonPlans(staff, 0, 10, null, null, null, null));

        assertEquals("Only teacher can view lesson plans", exception.getMessage());
    }

    @Test
    void getMyLessonPlansRejectsInvalidStatus() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(new RepositoryState()));
        User teacher = buildUser(7L, Role.RoleName.TEACHER);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonPlanService.getMyLessonPlans(teacher, 0, 10, "ARCHIVED", null, null, null));

        assertTrue(exception.getMessage().contains("Invalid status: ARCHIVED"));
    }

    @Test
    void getMyLessonPlansRejectsOversizedPage() {
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(new RepositoryState()));
        User teacher = buildUser(7L, Role.RoleName.TEACHER);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> lessonPlanService.getMyLessonPlans(teacher, 0, 101, null, null, null, null));

        assertEquals("size must not exceed 100", exception.getMessage());
    }

    @Test
    void createLessonPlanCreatesDraftOwnedByTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        LessonPlanRequest request = LessonPlanRequest.builder()
                .frameworkId(4)
                .title("  Introduction to Acids  ")
                .subject(" Chemistry ")
                .gradeLevel(" 10 ")
                .topic(" Acid Base ")
                .objectives(" Understand acids ")
                .activities(" Experiment ")
                .assessment(" Quiz ")
                .materials(" Beaker ")
                .durationMinutes(45)
                .build();

        LessonPlanDTO response = lessonPlanService.createLessonPlan(request, teacher);

        assertNotNull(state.lastSaved);
        assertEquals(teacher.getId(), state.lastSaved.getTeacher().getId());
        assertEquals("Introduction to Acids", state.lastSaved.getTitle());
        assertEquals("Chemistry", state.lastSaved.getSubject());
        assertEquals("10", state.lastSaved.getGradeLevel());
        assertEquals("Acid Base", state.lastSaved.getTopic());
        assertEquals("Understand acids", state.lastSaved.getObjectives());
        assertEquals("Experiment", state.lastSaved.getActivities());
        assertEquals("Quiz", state.lastSaved.getAssessment());
        assertEquals("Beaker", state.lastSaved.getMaterials());
        assertEquals(45, state.lastSaved.getDurationMinutes());
        assertEquals(LessonPlan.LessonPlanStatus.DRAFT, state.lastSaved.getStatus());
        assertEquals(false, state.lastSaved.getAiGenerated());
        assertNotNull(response.getId());
        assertEquals(teacher.getId(), response.getTeacherId());
        assertEquals(LessonPlan.LessonPlanStatus.DRAFT, response.getStatus());
    }

    @Test
    void getLessonPlanReturnsOwnedDetail() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        LessonPlan lessonPlan = buildLessonPlan(11L, teacher, LessonPlan.LessonPlanStatus.DRAFT, false);
        lessonPlan.setObjectives("Detailed objectives");
        state.store.put(lessonPlan.getId(), lessonPlan);
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        LessonPlanDTO response = lessonPlanService.getLessonPlan(11L, teacher);

        assertEquals(11L, response.getId());
        assertEquals("Detailed objectives", response.getObjectives());
        assertEquals(teacher.getId(), response.getTeacherId());
    }

    @Test
    void getLessonPlanRejectsOtherTeacher() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        User otherTeacher = buildUser(8L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.store.put(11L, buildLessonPlan(11L, otherTeacher, LessonPlan.LessonPlanStatus.DRAFT, false));
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> lessonPlanService.getLessonPlan(11L, teacher));

        assertEquals("You do not have permission to access this lesson plan", exception.getMessage());
    }

    @Test
    void getLessonPlanThrowsNotFoundWhenMissing() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(new RepositoryState()));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> lessonPlanService.getLessonPlan(999L, teacher));

        assertEquals("Lesson plan not found: 999", exception.getMessage());
    }

    @Test
    void updateLessonPlanUpdatesEditableFieldsAndPreservesManagedFields() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        LessonPlan existing = buildLessonPlan(15L, teacher, LessonPlan.LessonPlanStatus.PUBLISHED, true);
        existing.setTitle("Old title");
        existing.setCreatedAt(LocalDateTime.now().minusDays(2));
        state.store.put(existing.getId(), existing);
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        LessonPlanRequest request = LessonPlanRequest.builder()
                .frameworkId(6)
                .title("  New title  ")
                .subject(" Physics ")
                .gradeLevel(" 11 ")
                .topic(" Motion ")
                .objectives(" Learn motion ")
                .activities(" Practice ")
                .assessment(" Worksheet ")
                .materials(" Ball ")
                .durationMinutes(50)
                .build();

        LessonPlanDTO response = lessonPlanService.updateLessonPlan(15L, request, teacher);

        assertEquals("New title", existing.getTitle());
        assertEquals("Physics", existing.getSubject());
        assertEquals("11", existing.getGradeLevel());
        assertEquals("Motion", existing.getTopic());
        assertEquals("Learn motion", existing.getObjectives());
        assertEquals("Practice", existing.getActivities());
        assertEquals("Worksheet", existing.getAssessment());
        assertEquals("Ball", existing.getMaterials());
        assertEquals(50, existing.getDurationMinutes());
        assertEquals(LessonPlan.LessonPlanStatus.PUBLISHED, existing.getStatus());
        assertTrue(existing.getAiGenerated());
        assertEquals(teacher.getId(), existing.getTeacher().getId());
        assertEquals(LessonPlan.LessonPlanStatus.PUBLISHED, response.getStatus());
        assertTrue(response.getAiGenerated());
    }

    @Test
    void deleteLessonPlanRemovesOwnedPlan() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        RepositoryState state = new RepositoryState();
        state.store.put(20L, buildLessonPlan(20L, teacher, LessonPlan.LessonPlanStatus.DRAFT, false));
        LessonPlanService lessonPlanService = new LessonPlanService(createRepository(state));

        lessonPlanService.deleteLessonPlan(20L, teacher);

        assertEquals(20L, state.deletedId);
        assertNull(state.store.get(20L));
    }

    private LessonPlanRepository createRepository(RepositoryState state) {
        return (LessonPlanRepository) Proxy.newProxyInstance(
                LessonPlanRepository.class.getClassLoader(),
                new Class<?>[]{LessonPlanRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "findByTeacherIdWithFilters":
                            state.teacherId = (Long) args[0];
                            state.status = (LessonPlan.LessonPlanStatus) args[1];
                            state.subject = (String) args[2];
                            state.gradeLevel = (String) args[3];
                            state.keyword = (String) args[4];
                            state.pageable = (Pageable) args[5];
                            return state.listResponse != null ? state.listResponse : Page.empty();
                        case "findById":
                            return Optional.ofNullable(state.store.get((Long) args[0]));
                        case "save":
                            LessonPlan lessonPlan = (LessonPlan) args[0];
                            if (lessonPlan.getId() == null) {
                                lessonPlan.setId(state.nextId++);
                                if (lessonPlan.getCreatedAt() == null) {
                                    lessonPlan.setCreatedAt(LocalDateTime.now());
                                }
                            }
                            lessonPlan.setUpdatedAt(LocalDateTime.now());
                            state.lastSaved = lessonPlan;
                            state.store.put(lessonPlan.getId(), lessonPlan);
                            return lessonPlan;
                        case "delete":
                            LessonPlan toDelete = (LessonPlan) args[0];
                            state.deletedId = toDelete.getId();
                            state.store.remove(toDelete.getId());
                            return null;
                        case "toString":
                            return "LessonPlanRepositoryProxy";
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            throw new UnsupportedOperationException(method.getName());
                    }
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

    private LessonPlan buildLessonPlan(Long id, User teacher, LessonPlan.LessonPlanStatus status, boolean aiGenerated) {
        return LessonPlan.builder()
                .id(id)
                .teacher(teacher)
                .frameworkId(3)
                .title("Lesson plan")
                .subject("Chemistry")
                .gradeLevel("10")
                .topic("Topic")
                .objectives("Objectives")
                .activities("Activities")
                .assessment("Assessment")
                .materials("Materials")
                .durationMinutes(45)
                .aiGenerated(aiGenerated)
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static final class RepositoryState {
        private final Map<Long, LessonPlan> store = new HashMap<>();
        private long nextId = 100L;
        private Page<LessonPlanListItemDTO> listResponse;
        private Long teacherId;
        private LessonPlan.LessonPlanStatus status;
        private String subject;
        private String gradeLevel;
        private String keyword;
        private Pageable pageable;
        private LessonPlan lastSaved;
        private Long deletedId;
    }
}
