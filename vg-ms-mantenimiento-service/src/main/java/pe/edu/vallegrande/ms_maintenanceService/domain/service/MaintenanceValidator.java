package pe.edu.vallegrande.ms_maintenanceService.domain.service;

import java.time.LocalDate;
import java.math.BigDecimal;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceValidationException;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;

public class MaintenanceValidator {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999.99");

    public void validateCreation(Maintenance maintenance) {
        if (maintenance == null) {
            throw new MaintenanceValidationException("Los datos del mantenimiento son obligatorios");
        }
        if (maintenance.getReportedProblem() == null || maintenance.getReportedProblem().trim().isEmpty()) {
            throw new MaintenanceValidationException("El problema reportado es obligatorio");
        }
        if (maintenance.getScheduledDate() == null) {
            throw new MaintenanceValidationException("La fecha programada es obligatoria");
        }
        
        validateFutureDate(maintenance.getScheduledDate());
        validateAmount(maintenance.getLaborCost(), "mano de obra");
        
        if (Boolean.TRUE.equals(maintenance.getHasWarranty()) && maintenance.getWarrantyExpirationDate() == null) {
            throw new MaintenanceValidationException("La fecha de expiración de garantía es obligatoria");
        }
    }

    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new MaintenanceValidationException("El costo de " + fieldName + " no puede ser negativo");
            }
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                throw new MaintenanceValidationException("El costo de " + fieldName + " excede el límite permitido");
            }
        }
    }

    public void validateStateTransition(String currentState, String newState) {
        if (currentState == null || newState == null) {
            throw new MaintenanceValidationException("Los estados no pueden ser nulos");
        }

        if ("CONFIRMED".equals(currentState) || "CANCELLED".equals(currentState)) {
            throw new MaintenanceValidationException("No se puede cambiar el estado de un mantenimiento finalizado (" + currentState + ")");
        }

        boolean isValid = switch (currentState) {
            case "SCHEDULED" -> "IN_PROCESS".equals(newState) || "CANCELLED".equals(newState);
            case "IN_PROCESS" -> "PENDING_CONFORMITY".equals(newState) || "SUSPENDED".equals(newState) || "CANCELLED".equals(newState);
            case "PENDING_CONFORMITY" -> "CONFIRMED".equals(newState);
            case "SUSPENDED" -> "SCHEDULED".equals(newState) || "CANCELLED".equals(newState);
            default -> false;
        };

        if (!isValid) {
            throw new MaintenanceValidationException(String.format("Transición de estado no permitida: %s → %s", currentState, newState));
        }
    }

    public void validateCompletion(String appliedSolution, BigDecimal laborCost) {
        if (appliedSolution == null || appliedSolution.trim().isEmpty()) {
            throw new MaintenanceValidationException("La solución aplicada es obligatoria");
        }
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new MaintenanceValidationException("El costo de mano de obra debe ser positivo");
        }
    }

    public void validateFutureDate(LocalDate date) {
        if (date == null) return;
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new MaintenanceValidationException("No se permiten fechas en el pasado");
        }
    }

    public void validateReason(String observations, String action) {
        if (observations == null || observations.trim().isEmpty()) {
            throw new MaintenanceValidationException("Debe proporcionar un motivo para " + action);
        }
    }
}
