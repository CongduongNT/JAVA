package com.planbookai.backend.controller;

import com.planbookai.backend.BackendApplication;
import com.planbookai.backend.model.entity.LessonPlan;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import com.planbookai.backend.repository.LessonPlanRepository;
import com.planbookai.backend.repository.RoleRepository;
import com.planbookai.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("Blocked by Spring Boot 3.4.3 / Spring 6.2 metadata parsing on Java 25 class files in this environment")
class LessonPlanPublishIntegrationTest {

    private ConfigurableApplicationContext context;
    private MockMvc mockMvc;
    private LessonPlanRepository lessonPlanRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @BeforeAll
    void startApplication() {
        System.setProperty("spring.classformat.ignore", "true");
        context = new SpringApplicationBuilder(BackendApplication.class)
                .profiles("test")
                .properties("app.seed.enabled=false")
                .run();

        WebApplicationContext webApplicationContext = context.getBean(WebApplicationContext.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        lessonPlanRepository = context.getBean(LessonPlanRepository.class);
        userRepository = context.getBean(UserRepository.class);
        roleRepository = context.getBean(RoleRepository.class);
    }

    @AfterAll
    void stopApplication() {
        if (context != null) {
            context.close();
        }
    }

    @BeforeEach
    void cleanDatabase() {
        lessonPlanRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void publishDraftLessonPlanReturnsPublishedStatus() throws Exception {
        User teacher = saveTeacher();
        LessonPlan lessonPlan = lessonPlanRepository.save(buildCompleteLessonPlan(teacher, LessonPlan.LessonPlanStatus.DRAFT));

        mockMvc.perform(put("/api/v1/lesson-plans/{id}/publish", lessonPlan.getId())
                        .with(authenticatedTeacher(teacher))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(lessonPlan.getId()))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        LessonPlan persisted = lessonPlanRepository.findById(lessonPlan.getId()).orElseThrow();
        Assertions.assertEquals(LessonPlan.LessonPlanStatus.PUBLISHED, persisted.getStatus());
    }

    @Test
    void publishIncompleteDraftReturnsBadRequest() throws Exception {
        User teacher = saveTeacher();
        LessonPlan lessonPlan = buildCompleteLessonPlan(teacher, LessonPlan.LessonPlanStatus.DRAFT);
        lessonPlan.setAssessment(" ");
        lessonPlan = lessonPlanRepository.save(lessonPlan);

        mockMvc.perform(put("/api/v1/lesson-plans/{id}/publish", lessonPlan.getId())
                        .with(authenticatedTeacher(teacher))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Lesson plan is not ready to publish. Missing required fields: assessment"));
    }

    @Test
    void publishOtherTeacherLessonPlanReturnsForbidden() throws Exception {
        User owner = saveTeacher();
        User otherTeacher = saveTeacher();
        LessonPlan lessonPlan = lessonPlanRepository.save(buildCompleteLessonPlan(owner, LessonPlan.LessonPlanStatus.DRAFT));

        mockMvc.perform(put("/api/v1/lesson-plans/{id}/publish", lessonPlan.getId())
                        .with(authenticatedTeacher(otherTeacher))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this lesson plan"));
    }

    @Test
    void publishMissingLessonPlanReturnsNotFound() throws Exception {
        User teacher = saveTeacher();

        mockMvc.perform(put("/api/v1/lesson-plans/{id}/publish", 999999L)
                        .with(authenticatedTeacher(teacher))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson plan not found: 999999"));
    }

    private RequestPostProcessor authenticatedTeacher(User teacher) {
        return authentication(new UsernamePasswordAuthenticationToken(teacher, null, teacher.getAuthorities()));
    }

    private User saveTeacher() {
        Role teacherRole = roleRepository.findByName(Role.RoleName.TEACHER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(Role.RoleName.TEACHER)
                        .description("Teacher role")
                        .build()));

        return userRepository.save(User.builder()
                .role(teacherRole)
                .fullName("Teacher " + UUID.randomUUID())
                .email("teacher-" + UUID.randomUUID() + "@planbookai.com")
                .passwordHash("encoded")
                .isActive(true)
                .emailVerified(true)
                .build());
    }

    private LessonPlan buildCompleteLessonPlan(User teacher, LessonPlan.LessonPlanStatus status) {
        return LessonPlan.builder()
                .teacher(teacher)
                .frameworkId(1)
                .title("Complete lesson")
                .subject("Chemistry")
                .gradeLevel("10")
                .topic("Acids and bases")
                .objectives("Understand acid base theory")
                .activities("Perform lab activity")
                .assessment("Complete worksheet")
                .materials("Lab kit")
                .durationMinutes(45)
                .aiGenerated(false)
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
