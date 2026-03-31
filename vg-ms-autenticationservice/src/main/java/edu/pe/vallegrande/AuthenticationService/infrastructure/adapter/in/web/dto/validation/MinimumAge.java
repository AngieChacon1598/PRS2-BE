package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Anotación para validar edad mínima
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumAgeValidator.class)
@Documented
public @interface MinimumAge {

    String message() default "Debe ser mayor de edad (18 años)";

    int value() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
