package pe.edu.vallegrande.patrimonio_service.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(value = "asset_disposals")
public class AssetDisposal {
    @Id
    @Column("id")
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("file_number")
    private String fileNumber;

    // General information
    @Column("disposal_type")
    private String disposalType;

    @Column("disposal_reason")
    private String disposalReason;

    @Column("reason_description")
    private String reasonDescription;

    // Process dates
    @Column("request_date")
    private LocalDate requestDate;

    @Column("technical_evaluation_date")
    private LocalDate technicalEvaluationDate;

    @Column("resolution_date")
    private LocalDate resolutionDate;

    @Column("physical_removal_date")
    private LocalDate physicalRemovalDate;

    // Technical report author
    @Column("technical_report_author_id")
    private UUID technicalReportAuthorId;

    @Column("technical_report")
    private String technicalReport;

    // Approval
    @Column("approved_by_id")
    private UUID approvedById;

    @Column("approval_date")
    private LocalDate approvalDate;

    @Column("file_status")
    private String fileStatus;

    // File documents
    @Column("resolution_number")
    private String resolutionNumber;

    @Column("supporting_documents")
    private String supportingDocuments;

    // Additional information
    @Column("observations")
    private String observations;

    @Column("requires_destruction")
    private Boolean requiresDestruction;

    @Column("allows_donation")
    private Boolean allowsDonation;

    @Column("recoverable_value")
    private BigDecimal recoverableValue;

    // Audit fields
    @Column("requested_by")
    private UUID requestedBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private UUID updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
