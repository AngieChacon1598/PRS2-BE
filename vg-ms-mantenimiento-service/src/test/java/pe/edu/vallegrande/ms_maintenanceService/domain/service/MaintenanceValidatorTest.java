package pe.edu.vallegrande.ms_maintenanceService.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceValidationException;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;

class MaintenanceValidatorTest {

    private MaintenanceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MaintenanceValidator();
    }

    @Test
    void validarCreacion_CamposValidos_NoLanzaExcepcion() {
        Maintenance maintenance = Maintenance.builder()
                .assetId(UUID.randomUUID())
                .maintenanceCode("MT-001")
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(LocalDate.now().plusDays(1))
                .workDescription("Desc")
                .reportedProblem("Prob")
                .technicalResponsibleId(UUID.randomUUID())
                .requestedBy(UUID.randomUUID())
                .build();

        assertDoesNotThrow(() -> validator.validateCreation(maintenance));
    }

    @Test
    void validarCreacion_FaltaCodigo_LanzaExcepcion() {
        Maintenance maintenance = Maintenance.builder()
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .build();

        assertThrows(MaintenanceValidationException.class, () -> validator.validateCreation(maintenance));
    }

    @ParameterizedTest
    @CsvSource({
        "SCHEDULED, IN_PROCESS",
        "IN_PROCESS, PENDING_CONFORMITY",
        "PENDING_CONFORMITY, CONFIRMED",
        "SCHEDULED, CANCELLED",
        "IN_PROCESS, SUSPENDED"
    })
    void validarTransicion_Valida_NoLanzaExcepcion(String current, String next) {
        assertDoesNotThrow(() -> validator.validateStateTransition(current, next));
    }

    @Test
    void validarTransicion_Invalida_LanzaExcepcion() {
        assertThrows(MaintenanceValidationException.class, 
            () -> validator.validateStateTransition("CONFIRMED", "IN_PROCESS"));
    }

    @Test
    void validarFinalizacion_CamposValidos_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> validator.validateCompletion("Se reparó el motor", new BigDecimal("150.00")));
    }

    @Test
    void validarFinalizacion_CamposNulos_LanzaExcepcion() {
        assertThrows(MaintenanceValidationException.class, 
            () -> validator.validateCompletion(null, BigDecimal.TEN));
    }

    @Test
    void validarFinalizacion_SolucionVacia_LanzaExcepcion() {
        assertThrows(MaintenanceValidationException.class, 
            () -> validator.validateCompletion("", BigDecimal.TEN));
    }

    @Test
    void validarFinalizacion_CostoNegativo_LanzaExcepcion() {
        assertThrows(MaintenanceValidationException.class, 
            () -> validator.validateCompletion("Solucion", new BigDecimal("-1")));
    }

    @Test
    void validarFinalizacion_CostoCero_NoLanzaExcepcion() {
        assertDoesNotThrow(() -> validator.validateCompletion("Solucion", BigDecimal.ZERO));
    }
}