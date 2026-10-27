package com.pfe.management.dto;

import com.pfe.management.dto.request.ProjectCreateRequest;
import com.pfe.management.entity.ProjectStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Validation réussie avec un titre valide")
    void validDtoShouldHaveNoViolations() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Système de gestion de PFE",
                "Description du projet",
                ProjectStatus.PROPOSED,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<ProjectCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Échec de validation si le titre est blanc ou vide")
    void blankTitleShouldProduceViolation() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "   ",
                "Description",
                ProjectStatus.PROPOSED,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<ProjectCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("Échec de validation si le titre est trop court (< 3 caractères)")
    void tooShortTitleShouldProduceViolation() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "AB",
                "Description",
                ProjectStatus.PROPOSED,
                LocalDate.now(),
                LocalDate.now().plusMonths(6)
        );

        Set<ConstraintViolation<ProjectCreateRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
