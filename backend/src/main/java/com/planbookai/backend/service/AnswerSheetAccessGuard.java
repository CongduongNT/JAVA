package com.planbookai.backend.service;

import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AnswerSheetAccessGuard {

    public void requireTeacher(User user, String forbiddenMessage) {
        requireAuthenticatedUser(user);
        if (hasRole(user, Role.RoleName.TEACHER)) {
            return;
        }
        throw new ForbiddenOperationException(forbiddenMessage);
    }

    public void requireOwnedExam(Exam exam, User user, String forbiddenMessage) {
        Long teacherId = exam.getTeacher() != null ? exam.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException(forbiddenMessage);
    }

    public void requireOwnedAnswerSheet(AnswerSheet answerSheet, User user, String forbiddenMessage) {
        Long teacherId = answerSheet.getTeacher() != null ? answerSheet.getTeacher().getId() : null;
        if (teacherId != null && teacherId.equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException(forbiddenMessage);
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
}
