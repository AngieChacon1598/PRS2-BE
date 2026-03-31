package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceWebRequest {

    private UUID municipalityId;

    @Size(max = 50)
    private String maintenanceCode;

    @NotNull(message = "El ID del activo es obligatorio")
    private UUID assetId;

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    @Pattern(regexp = "PREVENTIVE|CORRECTIVE|PREDICTIVE|EMERGENCY")
    private String maintenanceType;

    @NotBlank(message = "La prioridad es obligatoria")
    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL")
    private String priority;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate scheduledDate;

    @NotBlank(message = "La descripción del trabajo es obligatoria")
    private String workDescription;

    @NotBlank(message = "El problema reportado es obligatorio")
    private String reportedProblem;

    private String observations;

    @NotNull(message = "El ID del técnico responsable es obligatorio")
    private UUID technicalResponsibleId;

    private UUID serviceSupplierId;
    
    private UUID supervisorId;

    @DecimalMin(value = "0.0")
    private BigDecimal laborCost;

    @DecimalMin(value = "0.0")
    private BigDecimal additionalCost;

    @NotNull(message = "El campo de garantía es obligatorio")
    private Boolean hasWarranty;

    private LocalDate warrantyExpirationDate;

    private UUID requestedBy;
}
