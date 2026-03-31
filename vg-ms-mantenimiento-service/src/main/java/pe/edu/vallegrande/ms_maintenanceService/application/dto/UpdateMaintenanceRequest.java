package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.ms_maintenanceService.domain.valueobject.AttachedDocument;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UpdateMaintenanceRequest {

    private UUID municipalityId;

    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    private String maintenanceCode;

    private UUID assetId;

    @Pattern(regexp = "PREVENTIVE|CORRECTIVE|PREDICTIVE|EMERGENCY", message = "Tipo de mantenimiento inválido")
    private String maintenanceType;

    private Boolean isScheduled;

    @Pattern(regexp = "LOW|MEDIUM|HIGH|CRITICAL", message = "Prioridad inválida")
    private String priority;

    private LocalDate scheduledDate;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDate nextDate;

    private String workDescription;

    private String reportedProblem;

    private String appliedSolution;

    private String observations;

    private UUID technicalResponsibleId;

    private UUID serviceSupplierId;

    @DecimalMin(value = "0.0", message = "El costo de mano de obra no puede ser negativo")
    private BigDecimal laborCost;

    @DecimalMin(value = "0.0", message = "El costo de partes no puede ser negativo")
    private BigDecimal partsCost;

    @Pattern(regexp = "SCHEDULED|IN_PROCESS|COMPLETED|CANCELLED|SUSPENDED", message = "Estado inválido")
    private String maintenanceStatus;

    private String workOrder;

    private List<AttachedDocument> attachedDocuments;

    private Boolean hasWarranty;

    private LocalDate warrantyExpirationDate;

    @NotNull(message = "El ID del usuario que actualiza es obligatorio")
    private UUID updatedBy;
}

