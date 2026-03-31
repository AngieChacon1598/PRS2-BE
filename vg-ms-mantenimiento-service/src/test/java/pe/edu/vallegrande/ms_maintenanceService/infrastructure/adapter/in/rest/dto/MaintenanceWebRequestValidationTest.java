package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class MaintenanceWebRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void debeRequerirProblemaReportado() {
        MaintenanceWebRequest request = MaintenanceWebRequest.builder()
                .municipalityId(UUID.randomUUID())
                .maintenanceCode("MANT-100")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(java.time.LocalDate.now().plusDays(1))
                .workDescription("Trabajo preventivo")
                .technicalResponsibleId(UUID.randomUUID())
                .hasWarranty(false)
                .requestedBy(UUID.randomUUID())
                .build();

        Set<ConstraintViolation<MaintenanceWebRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(v -> "reportedProblem".equals(v.getPropertyPath().toString())));
    }

    @Test
    void debePermitirObservacionesNulas() {
        MaintenanceWebRequest request = MaintenanceWebRequest.builder()
                .municipalityId(UUID.randomUUID())
                .maintenanceCode("MANT-101")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(java.time.LocalDate.now().plusDays(1))
                .workDescription("Trabajo preventivo")
                .reportedProblem("Ruido excesivo")
                .technicalResponsibleId(UUID.randomUUID())
                .serviceSupplierId(UUID.randomUUID())
                .hasWarranty(false)
                .requestedBy(UUID.randomUUID())
                .observations(null)
                .build();

        Set<ConstraintViolation<MaintenanceWebRequest>> violations = validator.validate(request);

        assertFalse(violations.stream().anyMatch(v -> "observations".equals(v.getPropertyPath().toString())));
    }
}