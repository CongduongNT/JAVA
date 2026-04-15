package com.planbookai.backend.service;

import com.planbookai.backend.exception.ForbiddenOperationException;
import com.planbookai.backend.model.entity.AnswerSheet;
import com.planbookai.backend.model.entity.Exam;
import com.planbookai.backend.model.entity.Role;
import com.planbookai.backend.model.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnswerSheetAccessGuardTest {

    private final AnswerSheetAccessGuard accessGuard = new AnswerSheetAccessGuard();

    @Test
    void requireTeacherAllowsTeacherUser() {
        assertDoesNotThrow(() -> accessGuard.requireTeacher(buildUser(7L, Role.RoleName.TEACHER), "Only teacher can access answer sheets"));
    }

    @Test
    void requireTeacherRejectsUnauthenticatedUser() {
        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> accessGuard.requireTeacher(null, "Only teacher can access answer sheets"));

        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void requireTeacherRejectsNonTeacherUser() {
        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> accessGuard.requireTeacher(buildUser(7L, Role.RoleName.STAFF), "Only teacher can access answer sheets"));

        assertEquals("Only teacher can access answer sheets", exception.getMessage());
    }

    @Test
    void requireOwnedExamRejectsOtherTeacherExam() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        Exam exam = buildExam(11L, buildUser(8L, Role.RoleName.TEACHER));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> accessGuard.requireOwnedExam(exam, teacher, "You do not have permission to access answer sheets for this exam"));

        assertEquals("You do not have permission to access answer sheets for this exam", exception.getMessage());
    }

    @Test
    void requireOwnedAnswerSheetRejectsOtherTeacherAnswerSheet() {
        User teacher = buildUser(7L, Role.RoleName.TEACHER);
        AnswerSheet answerSheet = new AnswerSheet();
        answerSheet.setTeacher(buildUser(8L, Role.RoleName.TEACHER));
        answerSheet.setExam(buildExam(11L, buildUser(8L, Role.RoleName.TEACHER)));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> accessGuard.requireOwnedAnswerSheet(answerSheet, teacher, "You do not have permission to access this answer sheet"));

        assertEquals("You do not have permission to access this answer sheet", exception.getMessage());
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

    private Exam buildExam(Long id, User teacher) {
        return Exam.builder()
                .id(id)
                .teacher(teacher)
                .title("Exam")
                .subject("Chemistry")
                .gradeLevel("10")
                .topic("Atomic")
                .status(Exam.ExamStatus.DRAFT)
                .build();
    }
}
