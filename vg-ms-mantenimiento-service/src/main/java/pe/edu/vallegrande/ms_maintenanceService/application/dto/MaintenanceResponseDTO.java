package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceResponseDTO {
    private UUID id;
    private UUID municipalityId;
    private String maintenanceCode;
    private UUID assetId;
    private String maintenanceType;
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
    private UUID supervisorId;
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal additionalCost;
    private BigDecimal totalCost;
    private String maintenanceStatus;
    private String workOrder;
    private Boolean hasWarranty;
    private LocalDate warrantyExpirationDate;
    private UUID requestedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
