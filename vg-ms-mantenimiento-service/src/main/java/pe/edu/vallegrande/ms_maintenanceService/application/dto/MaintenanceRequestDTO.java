package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRequestDTO {
    private UUID municipalityId;
    private String maintenanceCode;
    private UUID assetId;
    private String maintenanceType;
    private String priority;
    private LocalDate scheduledDate;
    private String workDescription;
    private String reportedProblem;
    private String observations;
    private UUID technicalResponsibleId;
    private UUID serviceSupplierId;
    private BigDecimal laborCost;
    private BigDecimal additionalCost;
    private Boolean hasWarranty;
    private LocalDate warrantyExpirationDate;
    private UUID requestedBy;
    private UUID supervisorId;
}
