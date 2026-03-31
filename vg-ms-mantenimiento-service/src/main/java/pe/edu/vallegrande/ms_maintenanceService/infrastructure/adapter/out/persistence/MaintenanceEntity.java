package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("maintenances")
public class MaintenanceEntity {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("maintenance_code")
    private String maintenanceCode;

    @Column("asset_id")
    private UUID assetId;

    @Column("asset_code")
    private String assetCode;

    @Column("asset_description")
    private String assetDescription;

    @Column("maintenance_type")
    private String maintenanceType;

    @Column("is_scheduled")
    private Boolean isScheduled;

    @Column("priority")
    private String priority;

    @Column("scheduled_date")
    private LocalDate scheduledDate;

    @Column("start_date")
    private LocalDateTime startDate;

    @Column("end_date")
    private LocalDateTime endDate;

    @Column("next_date")
    private LocalDate nextDate;

    @Column("estimated_duration_hours")
    private BigDecimal estimatedDurationHours;

    @Column("work_description")
    private String workDescription;

    @Column("reported_problem")
    private String reportedProblem;

    @Column("applied_solution")
    private String appliedSolution;

    @Column("observations")
    private String observations;

    @Column("technical_responsible_id")
    private UUID technicalResponsibleId;

    @Column("supervisor_id")
    private UUID supervisorId;

    @Column("requested_by")
    private UUID requestedBy;

    @Column("service_supplier_id")
    private UUID serviceSupplierId;

    @Column("labor_cost")
    private BigDecimal laborCost;

    @Column("parts_cost")
    private BigDecimal partsCost;

    @Column("additional_cost")
    private BigDecimal additionalCost;

    @ReadOnlyProperty
    @Column("total_cost")
    private BigDecimal totalCost; // GENERATED ALWAYS IN DB

    @Column("maintenance_status")
    private String maintenanceStatus;

    @Column("work_order")
    private String workOrder;

    @Column("purchase_order")
    private String purchaseOrder;

    @Column("invoice_number")
    private String invoiceNumber;

    @Column("has_warranty")
    private Boolean hasWarranty;

    @Column("warranty_expiration_date")
    private LocalDate warrantyExpirationDate;

    @Column("warranty_description")
    private String warrantyDescription;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private UUID updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
