package com.planbookai.backend.service;

@FunctionalInterface
public interface LessonPlanFrameworkValidator {

    void validateFrameworkIdIfAvailable(Integer frameworkId);
}
