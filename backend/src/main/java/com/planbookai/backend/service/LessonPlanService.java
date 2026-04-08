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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class LessonPlanService {

    private final LessonPlanRepository lessonPlanRepository;

    public LessonPlanService(LessonPlanRepository lessonPlanRepository) {
        this.lessonPlanRepository = lessonPlanRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<LessonPlanListItemDTO> getMyLessonPlans(
            User user,
            Integer page,
            Integer size,
            String status,
            String subject,
            String gradeLevel,
            String keyword) {
        assertCanViewOwnLessonPlans(user);
        validatePageRequest(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<LessonPlanListItemDTO> lessonPlans = lessonPlanRepository.findByTeacherIdWithFilters(
                user.getId(),
                parseStatus(status),
                normalizeFilter(subject),
                normalizeFilter(gradeLevel),
                normalizeFilter(keyword),
                pageable);

        return PageResponse.from(lessonPlans);
    }

    @Transactional
    public LessonPlanDTO createLessonPlan(LessonPlanRequest request, User user) {
        assertCanViewOwnLessonPlans(user);

        LessonPlan lessonPlan = LessonPlan.builder()
                .teacher(user)
                .frameworkId(request.getFrameworkId())
                .title(normalizeRequiredText(request.getTitle(), "title"))
                .subject(normalizeOptionalText(request.getSubject()))
                .gradeLevel(normalizeOptionalText(request.getGradeLevel()))
                .topic(normalizeOptionalText(request.getTopic()))
                .objectives(normalizeOptionalText(request.getObjectives()))
                .activities(normalizeOptionalText(request.getActivities()))
                .assessment(normalizeOptionalText(request.getAssessment()))
                .materials(normalizeOptionalText(request.getMaterials()))
                .durationMinutes(request.getDurationMinutes())
                .aiGenerated(false)
                .status(LessonPlan.LessonPlanStatus.DRAFT)
                .build();

        return mapToDTO(lessonPlanRepository.save(lessonPlan));
    }

    @Transactional(readOnly = true)
    public LessonPlanDTO getLessonPlan(Long id, User user) {
        LessonPlan lessonPlan = findLessonPlanOrThrow(id);
        assertOwnsLessonPlan(lessonPlan, user);
        return mapToDTO(lessonPlan);
    }

    @Transactional
    public LessonPlanDTO updateLessonPlan(Long id, LessonPlanRequest request, User user) {
        LessonPlan lessonPlan = findLessonPlanOrThrow(id);
        assertOwnsLessonPlan(lessonPlan, user);

        lessonPlan.setFrameworkId(request.getFrameworkId());
        lessonPlan.setTitle(normalizeRequiredText(request.getTitle(), "title"));
        lessonPlan.setSubject(normalizeOptionalText(request.getSubject()));
        lessonPlan.setGradeLevel(normalizeOptionalText(request.getGradeLevel()));
        lessonPlan.setTopic(normalizeOptionalText(request.getTopic()));
        lessonPlan.setObjectives(normalizeOptionalText(request.getObjectives()));
        lessonPlan.setActivities(normalizeOptionalText(request.getActivities()));
        lessonPlan.setAssessment(normalizeOptionalText(request.getAssessment()));
        lessonPlan.setMaterials(normalizeOptionalText(request.getMaterials()));
        lessonPlan.setDurationMinutes(request.getDurationMinutes());

        return mapToDTO(lessonPlanRepository.save(lessonPlan));
    }

    @Transactional
    public void deleteLessonPlan(Long id, User user) {
        LessonPlan lessonPlan = findLessonPlanOrThrow(id);
        assertOwnsLessonPlan(lessonPlan, user);
        lessonPlanRepository.delete(lessonPlan);
    }

    private void assertCanViewOwnLessonPlans(User user) {
        requireAuthenticatedUser(user);
        if (hasRole(user, Role.RoleName.TEACHER)) {
            return;
        }
        throw new ForbiddenOperationException("Only teacher can view lesson plans");
    }

    private void assertOwnsLessonPlan(LessonPlan lessonPlan, User user) {
        assertCanViewOwnLessonPlans(user);
        Long teacherId = lessonPlan.getTeacher() != null ? lessonPlan.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to access this lesson plan");
    }

    private LessonPlan findLessonPlanOrThrow(Long id) {
        return lessonPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson plan not found: " + id));
    }

    private void requireAuthenticatedUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ForbiddenOperationException("Authentication is required");
        }
    }

    private boolean hasRole(User user, Role.RoleName roleName) {
        return user != null
                && user.getRole() != null
                && user.getRole().getName() == roleName;
    }

    private void validatePageRequest(Integer page, Integer size) {
        if (page == null || page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size == null || size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("size must not exceed 100");
        }
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeOptionalText(String value) {
        String normalized = normalizeFilter(value);
        return normalized != null ? normalized : null;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeFilter(value);
        if (normalized != null) {
            return normalized;
        }
        throw new IllegalArgumentException(fieldName + " is required");
    }

    private LessonPlan.LessonPlanStatus parseStatus(String value) {
        String normalizedValue = normalizeFilter(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return LessonPlan.LessonPlanStatus.valueOf(normalizedValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            String allowedValues = Arrays.stream(LessonPlan.LessonPlanStatus.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid status: " + value + ". Allowed values: " + allowedValues);
        }
    }

    private LessonPlanDTO mapToDTO(LessonPlan lessonPlan) {
        return LessonPlanDTO.builder()
                .id(lessonPlan.getId())
                .teacherId(lessonPlan.getTeacher() != null ? lessonPlan.getTeacher().getId() : null)
                .frameworkId(lessonPlan.getFrameworkId())
                .title(lessonPlan.getTitle())
                .subject(lessonPlan.getSubject())
                .gradeLevel(lessonPlan.getGradeLevel())
                .topic(lessonPlan.getTopic())
                .objectives(lessonPlan.getObjectives())
                .activities(lessonPlan.getActivities())
                .assessment(lessonPlan.getAssessment())
                .materials(lessonPlan.getMaterials())
                .durationMinutes(lessonPlan.getDurationMinutes())
                .aiGenerated(lessonPlan.getAiGenerated())
                .status(lessonPlan.getStatus())
                .createdAt(lessonPlan.getCreatedAt())
                .updatedAt(lessonPlan.getUpdatedAt())
                .build();
    }
}
