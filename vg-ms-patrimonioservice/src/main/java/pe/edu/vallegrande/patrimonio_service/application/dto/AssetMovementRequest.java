package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssetMovementRequest {

    private UUID municipalityId;
    private String movementNumber;
    private UUID assetId;

    // Tipo de movimiento: ASSET_DISPOSAL (baja)
    private String movementType;

    // Subtipo: DESTROY | DONATE | SELL | RECYCLE | TRANSFER
    private String movementSubtype;

    // Responsable origen (quien tenía el bien antes de la baja)
    private UUID originResponsibleId;

    // Responsable destino (quien ejecutó el retiro físico)
    private UUID destinationResponsibleId;

    // Área origen del bien antes de la baja
    private UUID originAreaId;

    // Área destino (null en baja, el bien sale del sistema)
    private UUID destinationAreaId;

    // Ubicación física origen del bien
    private UUID originLocationId;

    // Ubicación destino (null en baja)
    private UUID destinationLocationId;

    // Fechas del ciclo del movimiento
    private LocalDateTime requestDate;
    private LocalDateTime approvalDate;
    private LocalDateTime executionDate;

    // La aprobación ya fue gestionada en el expediente de baja
    private Boolean requiresApproval;

    // Estado final del movimiento en el MS de movimientos
    private String movementStatus;

    // ID del funcionario que aprobó la baja
    private UUID approvedBy;

    // Descripción del motivo de la baja
    private String reason;

    private String observations;

    private String specialConditions;

    private UUID requestingUser;
}
