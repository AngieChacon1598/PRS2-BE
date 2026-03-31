package pe.edu.vallegrande.movementservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetMovementResponse {

    private UUID id;
    private UUID municipalityId;
    private String movementNumber;
    private UUID assetId;
    private String movementType;
    private String movementSubtype;
    private UUID originResponsibleId;
    private UUID destinationResponsibleId;
    private UUID originAreaId;
    private UUID destinationAreaId;
    private UUID originLocationId;
    private UUID destinationLocationId;
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private LocalDateTime executionDate;
    private LocalDateTime receptionDate;
    private String movementStatus;
    private Boolean requiresApproval;
    private UUID approvedBy;
    private String reason;
    private String observations;
    private String specialConditions;
    private String supportingDocumentNumber;
    private String supportingDocumentType;
    private String attachedDocuments;
    private UUID requestingUser;
    private UUID executingUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
    private UUID deletedBy;
    private LocalDateTime deletedAt;
    private UUID restoredBy;
    private LocalDateTime restoredAt;
}
