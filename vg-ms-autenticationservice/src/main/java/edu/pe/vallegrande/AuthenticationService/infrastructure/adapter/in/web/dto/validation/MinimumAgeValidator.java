package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.validation;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación MinimumAge
 */
public class MinimumAgeValidator implements ConstraintValidator<MinimumAge, LocalDate> {

    private int minimumAge;

    @Override
    public void initialize(MinimumAge constraintAnnotation) {
        this.minimumAge = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // La validación de @NotNull se encarga de esto
        }

        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();

        return age >= minimumAge;
    }
}
