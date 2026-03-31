package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.movementservice.application.validation.ValidationGroups;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetMovementRequest {

    @NotNull(message = "municipalityId es obligatorio", groups = ValidationGroups.OnCreate.class)
    private UUID municipalityId;

    @Size(max = 64, message = "movementNumber no puede superar 64 caracteres")
    private String movementNumber;

    @NotNull(message = "assetId es obligatorio", groups = ValidationGroups.OnCreate.class)
    private UUID assetId;

    @NotBlank(message = "movementType es obligatorio", groups = ValidationGroups.OnCreate.class)
    @Size(max = 100, message = "movementType no puede superar 100 caracteres")
    private String movementType;

    @Size(max = 100, message = "movementSubtype no puede superar 100 caracteres")
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

    @Size(max = 50, message = "movementStatus no puede superar 50 caracteres")
    private String movementStatus;
    private Boolean requiresApproval;
    private UUID approvedBy;

    @NotBlank(message = "reason es obligatorio", groups = ValidationGroups.OnCreate.class)
    @Size(max = 2000, message = "reason no puede superar 2000 caracteres")
    private String reason;

    @Size(max = 4000, message = "observations no puede superar 4000 caracteres")
    private String observations;

    @Size(max = 2000, message = "specialConditions no puede superar 2000 caracteres")
    private String specialConditions;

    @Size(max = 255, message = "supportingDocumentNumber no puede superar 255 caracteres")
    private String supportingDocumentNumber;

    @Size(max = 255, message = "supportingDocumentType no puede superar 255 caracteres")
    private String supportingDocumentType;

    @Size(max = 50000, message = "attachedDocuments no puede superar 50000 caracteres")
    private String attachedDocuments;

    @NotNull(message = "requestingUser es obligatorio", groups = ValidationGroups.OnCreate.class)
    private UUID requestingUser;
    private UUID executingUser;
}
