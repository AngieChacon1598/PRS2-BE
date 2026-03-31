package pe.edu.vallegrande.ms_maintenanceService.domain.model;

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
public class Maintenance {

    private UUID id;
    private UUID municipalityId;
    private String maintenanceCode;
    
    // Referencias Externas: PatrimonioService
    private UUID assetId;
    private String assetCode;
    private String assetDescription;

    private String maintenanceType;
    private Boolean isScheduled;
    private String priority;

    private LocalDate scheduledDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDate nextDate;
    private BigDecimal estimatedDurationHours;

    private String workDescription;
    private String reportedProblem;
    private String appliedSolution;
    private String observations;

    // Referencias Externas: AuthService
    private UUID technicalResponsibleId;
    private UUID supervisorId;
    private UUID requestedBy;

    // Referencias Externas: ConfigService
    private UUID serviceSupplierId;
    
    private BigDecimal laborCost;
    private BigDecimal partsCost;
    private BigDecimal additionalCost;
    private BigDecimal totalCost;

    private String maintenanceStatus;

    private String workOrder;
    private String purchaseOrder;
    private String invoiceNumber;

    private Boolean hasWarranty;
    private LocalDate warrantyExpirationDate;
    private String warrantyDescription;

    private LocalDateTime createdAt;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
}

