package com.planbookai.backend.controller;

import com.planbookai.backend.dto.LessonPlanDTO;
import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.LessonPlanRequest;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.model.entity.LessonPlan;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.LessonPlanRepository;
import com.planbookai.backend.service.AiPromptTemplateService;
import com.planbookai.backend.service.LessonPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LessonPlanControllerTest {

    @Test
    void getMyLessonPlansDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        PageResponse<LessonPlanListItemDTO> expected = new PageResponse<>(
                List.of(LessonPlanListItemDTO.builder().id(1L).title("Acids").build()),
                0,
                10,
                1,
                1,
                true,
                true,
                false);
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        service.listResponse = expected;
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<PageResponse<LessonPlanListItemDTO>> response = controller.getMyLessonPlans(
                0,
                10,
                "DRAFT",
                "Chemistry",
                "10",
                "Atomic",
                teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertSame(teacher, service.user);
        assertEquals(0, service.page);
        assertEquals(10, service.size);
        assertEquals("DRAFT", service.status);
        assertEquals("Chemistry", service.subject);
        assertEquals("10", service.gradeLevel);
        assertEquals("Atomic", service.keyword);
    }

    @Test
    void createLessonPlanDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        LessonPlanRequest request = LessonPlanRequest.builder().title("Lesson").build();
        LessonPlanDTO expected = LessonPlanDTO.builder().id(10L).title("Lesson").status(LessonPlan.LessonPlanStatus.DRAFT).build();
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        service.detailResponse = expected;
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<LessonPlanDTO> response = controller.createLessonPlan(request, teacher);

        assertEquals(201, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertSame(request, service.request);
        assertSame(teacher, service.user);
    }

    @Test
    void getLessonPlanDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        LessonPlanDTO expected = LessonPlanDTO.builder().id(10L).title("Lesson").build();
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        service.detailResponse = expected;
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<LessonPlanDTO> response = controller.getLessonPlan(10L, teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(10L, service.id);
        assertSame(teacher, service.user);
    }

    @Test
    void updateLessonPlanDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        LessonPlanRequest request = LessonPlanRequest.builder().title("Lesson").build();
        LessonPlanDTO expected = LessonPlanDTO.builder().id(10L).title("Lesson").build();
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        service.detailResponse = expected;
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<LessonPlanDTO> response = controller.updateLessonPlan(10L, request, teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(10L, service.id);
        assertSame(request, service.request);
        assertSame(teacher, service.user);
    }

    @Test
    void deleteLessonPlanDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<Void> response = controller.deleteLessonPlan(10L, teacher);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
        assertEquals(10L, service.id);
        assertSame(teacher, service.user);
        assertTrue(service.deleteCalled);
    }

    @Test
    void publishLessonPlanDelegatesToService() {
        User teacher = User.builder().id(5L).build();
        LessonPlanDTO expected = LessonPlanDTO.builder().id(10L).status(LessonPlan.LessonPlanStatus.PUBLISHED).build();
        RecordingLessonPlanService service = new RecordingLessonPlanService();
        service.detailResponse = expected;
        LessonPlanController controller = new LessonPlanController(null, service);

        ResponseEntity<LessonPlanDTO> response = controller.publishLessonPlan(10L, teacher);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(10L, service.id);
        assertSame(teacher, service.user);
        assertTrue(service.publishCalled);
    }

    @Test
    void lessonPlanEndpointsRequireTeacherRole() throws NoSuchMethodException {
        assertTeacherRole("getMyLessonPlans",
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                String.class,
                User.class);
        assertTeacherRole("createLessonPlan", LessonPlanRequest.class, User.class);
        assertTeacherRole("getLessonPlan", Long.class, User.class);
        assertTeacherRole("updateLessonPlan", Long.class, LessonPlanRequest.class, User.class);
        assertTeacherRole("deleteLessonPlan", Long.class, User.class);
        assertTeacherRole("publishLessonPlan", Long.class, User.class);
    }

    private void assertTeacherRole(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = LessonPlanController.class.getMethod(methodName, parameterTypes);
        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(preAuthorize);
        assertEquals("hasRole('TEACHER')", preAuthorize.value());
    }

    private static final class RecordingLessonPlanService extends LessonPlanService {
        private PageResponse<LessonPlanListItemDTO> listResponse;
        private LessonPlanDTO detailResponse;
        private User user;
        private Integer page;
        private Integer size;
        private String status;
        private String subject;
        private String gradeLevel;
        private String keyword;
        private Long id;
        private LessonPlanRequest request;
        private boolean deleteCalled;
        private boolean publishCalled;

        private RecordingLessonPlanService() {
            super(createNoOpRepository());
        }

        @Override
        public PageResponse<LessonPlanListItemDTO> getMyLessonPlans(
                User user,
                Integer page,
                Integer size,
                String status,
                String subject,
                String gradeLevel,
                String keyword) {
            this.user = user;
            this.page = page;
            this.size = size;
            this.status = status;
            this.subject = subject;
            this.gradeLevel = gradeLevel;
            this.keyword = keyword;
            return listResponse;
        }

        @Override
        public LessonPlanDTO createLessonPlan(LessonPlanRequest request, User user) {
            this.request = request;
            this.user = user;
            return detailResponse;
        }

        @Override
        public LessonPlanDTO getLessonPlan(Long id, User user) {
            this.id = id;
            this.user = user;
            return detailResponse;
        }

        @Override
        public LessonPlanDTO updateLessonPlan(Long id, LessonPlanRequest request, User user) {
            this.id = id;
            this.request = request;
            this.user = user;
            return detailResponse;
        }

        @Override
        public void deleteLessonPlan(Long id, User user) {
            this.id = id;
            this.user = user;
            this.deleteCalled = true;
        }

        @Override
        public LessonPlanDTO publishLessonPlan(Long id, User user) {
            this.id = id;
            this.user = user;
            this.publishCalled = true;
            return detailResponse;
        }
    }

    private static LessonPlanRepository createNoOpRepository() {
        return (LessonPlanRepository) Proxy.newProxyInstance(
                LessonPlanRepository.class.getClassLoader(),
                new Class<?>[]{LessonPlanRepository.class},
                (proxy, method, args) -> {
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

    private static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}
