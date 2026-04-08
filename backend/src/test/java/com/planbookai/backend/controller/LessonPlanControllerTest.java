package com.planbookai.backend.controller;

import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.LessonPlanRepository;
import com.planbookai.backend.service.LessonPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        RecordingLessonPlanService service = new RecordingLessonPlanService(expected);
        LessonPlanController controller = new LessonPlanController(service);

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
    void getMyLessonPlansRequiresTeacherRole() throws NoSuchMethodException {
        Method method = LessonPlanController.class.getMethod(
                "getMyLessonPlans",
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                String.class,
                User.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('TEACHER')", preAuthorize.value());
    }

    private static final class RecordingLessonPlanService extends LessonPlanService {
        private final PageResponse<LessonPlanListItemDTO> response;
        private User user;
        private Integer page;
        private Integer size;
        private String status;
        private String subject;
        private String gradeLevel;
        private String keyword;

        private RecordingLessonPlanService(PageResponse<LessonPlanListItemDTO> response) {
            super(createNoOpRepository());
            this.response = response;
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
            return response;
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
}
