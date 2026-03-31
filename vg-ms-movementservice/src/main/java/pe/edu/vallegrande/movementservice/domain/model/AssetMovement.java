package pe.edu.vallegrande.movementservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("asset_movements")
public class AssetMovement {

    @Id
    @Column("id")
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("movement_number")
    private String movementNumber;

    @Column("asset_id")
    private UUID assetId;

    @Column("movement_type")
    private String movementType;

    @Column("movement_subtype")
    private String movementSubtype;

    @Column("origin_responsible_id")
    private UUID originResponsibleId;

    @Column("destination_responsible_id")
    private UUID destinationResponsibleId;

    @Column("origin_area_id")
    private UUID originAreaId;

    @Column("destination_area_id")
    private UUID destinationAreaId;

    @Column("origin_location_id")
    private UUID originLocationId;

    @Column("destination_location_id")
    private UUID destinationLocationId;

    @Column("request_date")
    private LocalDateTime requestDate;

    @Column("approval_date")
    private LocalDateTime approvalDate;

    @Column("execution_date")
    private LocalDateTime executionDate;

    @Column("reception_date")
    private LocalDateTime receptionDate;

    @Column("movement_status")
    private String movementStatus;

    @Column("requires_approval")
    @Builder.Default
    private Boolean requiresApproval = true;

    @Column("approved_by")
    private UUID approvedBy;

    @Column("reason")
    private String reason;

    @Column("observations")
    private String observations;

    @Column("special_conditions")
    private String specialConditions;

    @Column("supporting_document_number")
    private String supportingDocumentNumber;

    @Column("supporting_document_type")
    private String supportingDocumentType;

    @Column("attached_documents")
    private String attachedDocuments;

    @Column("requesting_user")
    private UUID requestingUser;

    @Column("executing_user")
    private UUID executingUser;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("active")
    @Builder.Default
    private Boolean active = true;

    @Column("deleted_by")
    private UUID deletedBy;

    @Column("deleted_at")
    private LocalDateTime deletedAt;

    @Column("restored_by")
    private UUID restoredBy;

    @Column("restored_at")
    private LocalDateTime restoredAt;
}
