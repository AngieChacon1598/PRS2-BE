package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteMaintenanceRequest {

    @Size(max = 100, message = "La orden de trabajo no puede exceder 100 caracteres")
    private String workOrder;

    @NotNull(message = "El costo de mano de obra es obligatorio")
    @DecimalMin(value = "0.0", message = "El costo de mano de obra no puede ser negativo")
    private BigDecimal laborCost;

    @NotBlank(message = "La solución aplicada es obligatoria")
    private String appliedSolution;

    private String observations;

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID updatedBy;
}
