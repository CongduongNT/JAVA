package com.planbookai.backend.service;

import com.planbookai.backend.dto.LessonPlanListItemDTO;
import com.planbookai.backend.dto.PageResponse;
import com.planbookai.backend.exception.ForbiddenOperationException;
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

    private void assertCanViewOwnLessonPlans(User user) {
        requireAuthenticatedUser(user);
        if (hasRole(user, Role.RoleName.TEACHER)) {
            return;
        }
        throw new ForbiddenOperationException("Only teacher can view lesson plans");
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
}
