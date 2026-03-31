package pe.edu.vallegrande.movementservice.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementResponse;
import pe.edu.vallegrande.movementservice.application.dto.AssetUpdateRequest;
import pe.edu.vallegrande.movementservice.application.dto.MovementNotificationRequest;
import pe.edu.vallegrande.movementservice.application.ports.input.AssetMovementServicePort;
import pe.edu.vallegrande.movementservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.movementservice.domain.model.AssetMovement;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.client.AssetServiceClient;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence.AssetMovementRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetMovementService implements AssetMovementServicePort {

    private final AssetMovementRepository assetMovementRepository;
    private final AssetServiceClient assetServiceClient;
    private final DatabaseClient databaseClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Mono<AssetMovementResponse> create(AssetMovementRequest request) {
        log.info("Creating asset movement for municipality: {}, assetId: {}", request.getMunicipalityId(), request.getAssetId());

        Mono<String> movementNumberMono;
        if (request.getMovementNumber() == null || request.getMovementNumber().trim().isEmpty()) {
            log.info("Movement number not provided, generating automatically for municipality: {}", request.getMunicipalityId());
            movementNumberMono = generateNextMovementNumber(request.getMunicipalityId());
        } else {
            movementNumberMono = assetMovementRepository.findByMovementNumberAndMunicipalityId(request.getMovementNumber(), request.getMunicipalityId())
                    .flatMap(existing -> Mono.<String>error(new IllegalStateException(
                            "Movement number " + request.getMovementNumber() + " already exists for this municipality")))
                    .switchIfEmpty(Mono.just(request.getMovementNumber()));
        }

        return movementNumberMono
                .flatMap(movementNumber -> {
                    final String finalMovementNumber = movementNumber;
                    
                    return Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    String attachedDocuments = validateAndNormalizeAttachedDocuments(request.getAttachedDocuments());
                    
                    LocalDateTime requestDate = request.getRequestDate() != null ? request.getRequestDate() : now;
                    String movementStatus = request.getMovementStatus() != null ? request.getMovementStatus() : "REQUESTED";
                    Boolean requiresApproval = request.getRequiresApproval() != null ? request.getRequiresApproval() : true;
                    
                    String finalAttachedDocuments = attachedDocuments != null ? attachedDocuments : "[]";
                    
                    DatabaseClient.GenericExecuteSpec sqlSpec = databaseClient.sql(
                            "INSERT INTO asset_movements (" +
                            "municipality_id, movement_number, asset_id, movement_type, movement_subtype, " +
                            "origin_responsible_id, destination_responsible_id, origin_area_id, destination_area_id, " +
                            "origin_location_id, destination_location_id, request_date, approval_date, execution_date, " +
                            "reception_date, movement_status, requires_approval, approved_by, reason, observations, " +
                            "special_conditions, supporting_document_number, supporting_document_type, attached_documents, " +
                            "requesting_user, executing_user, active, created_at) " +
                            "VALUES (" +
                            ":municipalityId, :movementNumber, :assetId, :movementType, :movementSubtype, " +
                            ":originResponsibleId, :destinationResponsibleId, :originAreaId, :destinationAreaId, " +
                            ":originLocationId, :destinationLocationId, :requestDate, :approvalDate, :executionDate, " +
                            ":receptionDate, :movementStatus, :requiresApproval, :approvedBy, :reason, :observations, " +
                            ":specialConditions, :supportingDocumentNumber, :supportingDocumentType, :attachedDocuments::jsonb, " +
                            ":requestingUser, :executingUser, :active, :createdAt) " +
                            "RETURNING *"
                    )
                    .bind("municipalityId", request.getMunicipalityId())
                    .bind("movementNumber", finalMovementNumber)
                    .bind("assetId", request.getAssetId())
                    .bind("movementType", request.getMovementType());
                    
                    sqlSpec = (request.getMovementSubtype() != null) ? 
                            sqlSpec.bind("movementSubtype", request.getMovementSubtype()) : 
                            sqlSpec.bindNull("movementSubtype", String.class);
                    sqlSpec = (request.getOriginResponsibleId() != null) ? 
                            sqlSpec.bind("originResponsibleId", request.getOriginResponsibleId()) : 
                            sqlSpec.bindNull("originResponsibleId", UUID.class);
                    sqlSpec = (request.getDestinationResponsibleId() != null) ? 
                            sqlSpec.bind("destinationResponsibleId", request.getDestinationResponsibleId()) : 
                            sqlSpec.bindNull("destinationResponsibleId", UUID.class);
                    sqlSpec = (request.getOriginAreaId() != null) ? 
                            sqlSpec.bind("originAreaId", request.getOriginAreaId()) : 
                            sqlSpec.bindNull("originAreaId", UUID.class);
                    sqlSpec = (request.getDestinationAreaId() != null) ? 
                            sqlSpec.bind("destinationAreaId", request.getDestinationAreaId()) : 
                            sqlSpec.bindNull("destinationAreaId", UUID.class);
                    sqlSpec = (request.getOriginLocationId() != null) ? 
                            sqlSpec.bind("originLocationId", request.getOriginLocationId()) : 
                            sqlSpec.bindNull("originLocationId", UUID.class);
                    sqlSpec = (request.getDestinationLocationId() != null) ? 
                            sqlSpec.bind("destinationLocationId", request.getDestinationLocationId()) : 
                            sqlSpec.bindNull("destinationLocationId", UUID.class);
                    sqlSpec = sqlSpec.bind("requestDate", requestDate);
                    sqlSpec = (request.getApprovalDate() != null) ? 
                            sqlSpec.bind("approvalDate", request.getApprovalDate()) : 
                            sqlSpec.bindNull("approvalDate", LocalDateTime.class);
                    sqlSpec = (request.getExecutionDate() != null) ? 
                            sqlSpec.bind("executionDate", request.getExecutionDate()) : 
                            sqlSpec.bindNull("executionDate", LocalDateTime.class);
                    sqlSpec = (request.getReceptionDate() != null) ? 
                            sqlSpec.bind("receptionDate", request.getReceptionDate()) : 
                            sqlSpec.bindNull("receptionDate", LocalDateTime.class);
                    sqlSpec = sqlSpec.bind("movementStatus", movementStatus);
                    sqlSpec = sqlSpec.bind("requiresApproval", requiresApproval);
                    sqlSpec = (request.getApprovedBy() != null) ? 
                            sqlSpec.bind("approvedBy", request.getApprovedBy()) : 
                            sqlSpec.bindNull("approvedBy", UUID.class);
                    sqlSpec = sqlSpec.bind("reason", request.getReason());
                    sqlSpec = (request.getObservations() != null) ? 
                            sqlSpec.bind("observations", request.getObservations()) : 
                            sqlSpec.bindNull("observations", String.class);
                    sqlSpec = (request.getSpecialConditions() != null) ? 
                            sqlSpec.bind("specialConditions", request.getSpecialConditions()) : 
                            sqlSpec.bindNull("specialConditions", String.class);
                    sqlSpec = (request.getSupportingDocumentNumber() != null) ? 
                            sqlSpec.bind("supportingDocumentNumber", request.getSupportingDocumentNumber()) : 
                            sqlSpec.bindNull("supportingDocumentNumber", String.class);
                    sqlSpec = (request.getSupportingDocumentType() != null) ? 
                            sqlSpec.bind("supportingDocumentType", request.getSupportingDocumentType()) : 
                            sqlSpec.bindNull("supportingDocumentType", String.class);
                    sqlSpec = sqlSpec.bind("attachedDocuments", finalAttachedDocuments);
                    sqlSpec = sqlSpec.bind("requestingUser", request.getRequestingUser());
                    sqlSpec = (request.getExecutingUser() != null) ? 
                            sqlSpec.bind("executingUser", request.getExecutingUser()) : 
                            sqlSpec.bindNull("executingUser", UUID.class);
                    sqlSpec = sqlSpec.bind("active", true);
                    sqlSpec = sqlSpec.bind("createdAt", now);
                    
                    return sqlSpec
                    .map((row, metadata) -> {
                        return AssetMovement.builder()
                                .id(row.get("id", UUID.class))
                                .municipalityId(row.get("municipality_id", UUID.class))
                                .movementNumber(row.get("movement_number", String.class))
                                .assetId(row.get("asset_id", UUID.class))
                                .movementType(row.get("movement_type", String.class))
                                .movementSubtype(row.get("movement_subtype", String.class))
                                .originResponsibleId(row.get("origin_responsible_id", UUID.class))
                                .destinationResponsibleId(row.get("destination_responsible_id", UUID.class))
                                .originAreaId(row.get("origin_area_id", UUID.class))
                                .destinationAreaId(row.get("destination_area_id", UUID.class))
                                .originLocationId(row.get("origin_location_id", UUID.class))
                                .destinationLocationId(row.get("destination_location_id", UUID.class))
                                .requestDate(row.get("request_date", LocalDateTime.class))
                                .approvalDate(row.get("approval_date", LocalDateTime.class))
                                .executionDate(row.get("execution_date", LocalDateTime.class))
                                .receptionDate(row.get("reception_date", LocalDateTime.class))
                                .movementStatus(row.get("movement_status", String.class))
                                .requiresApproval(row.get("requires_approval", Boolean.class))
                                .approvedBy(row.get("approved_by", UUID.class))
                                .reason(row.get("reason", String.class))
                                .observations(row.get("observations", String.class))
                                .specialConditions(row.get("special_conditions", String.class))
                                .supportingDocumentNumber(row.get("supporting_document_number", String.class))
                                .supportingDocumentType(row.get("supporting_document_type", String.class))
                                .attachedDocuments(row.get("attached_documents", String.class))
                                .requestingUser(row.get("requesting_user", UUID.class))
                                .executingUser(row.get("executing_user", UUID.class))
                                .createdAt(row.get("created_at", LocalDateTime.class))
                                .updatedAt(row.get("updated_at", LocalDateTime.class))
                                .active(row.get("active", Boolean.class))
                                .deletedBy(row.get("deleted_by", UUID.class))
                                .deletedAt(row.get("deleted_at", LocalDateTime.class))
                                .restoredBy(row.get("restored_by", UUID.class))
                                .restoredAt(row.get("restored_at", LocalDateTime.class))
                                .build();
                    })
                    .first()
                    .doOnError(error -> log.error("Error saving asset movement: {}", error.getMessage(), error));
                    });
                })
                .flatMap(savedMovement -> {
                    if ("REQUESTED".equals(savedMovement.getMovementStatus()) && 
                        savedMovement.getRequiresApproval() != null && 
                        savedMovement.getRequiresApproval()) {
                        
                        MovementNotificationRequest notification = MovementNotificationRequest.builder()
                                .movementId(savedMovement.getId())
                                .assetId(savedMovement.getAssetId())
                                .municipalityId(savedMovement.getMunicipalityId())
                                .movementNumber(savedMovement.getMovementNumber())
                                .movementType(savedMovement.getMovementType())
                                .movementStatus(savedMovement.getMovementStatus())
                                .requestingUser(savedMovement.getRequestingUser())
                                .reason(savedMovement.getReason())
                                .requestDate(savedMovement.getRequestDate())
                                .originResponsibleId(savedMovement.getOriginResponsibleId())
                                .destinationResponsibleId(savedMovement.getDestinationResponsibleId())
                                .originAreaId(savedMovement.getOriginAreaId())
                                .destinationAreaId(savedMovement.getDestinationAreaId())
                                .originLocationId(savedMovement.getOriginLocationId())
                                .destinationLocationId(savedMovement.getDestinationLocationId())
                                .build();
                        
                        assetServiceClient.notifyNewMovement(notification)
                                .subscribe(
                                        null,
                                        error -> log.warn("Failed to notify Assets Service about new movement: {}", 
                                                savedMovement.getId(), error)
                                );
                    }
                    return Mono.just(savedMovement);
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Asset movement created successfully with ID: {} and movement number: {}", m.getId(), m.getMovementNumber()));
    }

    private String validateAndNormalizeAttachedDocuments(String attachedDocuments) {
        if (attachedDocuments == null || attachedDocuments.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = attachedDocuments.trim();
        
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new IllegalArgumentException(
                    "attachedDocuments must be a valid JSON array. Example: [{\"fileName\":\"doc.pdf\",\"fileUrl\":\"https://...\"}]");
        }
        
        try {
            objectMapper.readValue(trimmed, List.class);
            return trimmed;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "attachedDocuments contains invalid JSON: " + e.getMessage() + 
                    ". Expected format: [{\"fileName\":\"string\",\"fileUrl\":\"string\"}]", e);
        }
    }

    private Mono<String> generateNextMovementNumber(UUID municipalityId) {
        return assetMovementRepository.findLastMovementNumberByMunicipalityId(municipalityId)
                .map(lastNumber -> {
                    try {
                        String numericPart = lastNumber.substring(3); 
                        int lastNumberInt = Integer.parseInt(numericPart);
                        int nextNumber = lastNumberInt + 1;
                        return String.format("MV-%05d", nextNumber);
                    } catch (Exception e) {
                        log.warn("Error parsing last movement number: {}. Starting from MV-00001", lastNumber, e);
                        return "MV-00001";
                    }
                })
                .defaultIfEmpty("MV-00001")
                .doOnNext(number -> log.info("Generated movement number: {} for municipality: {}", number, municipalityId));
    }

    public Flux<AssetMovementResponse> getAllByMunicipality(UUID municipalityId) {
        log.info("Getting all movements for municipality: {}", municipalityId);
        return assetMovementRepository.findByMunicipalityIdOrderByRequestDateDesc(municipalityId)
                .map(this::mapToResponse);
    }

    public Mono<AssetMovementResponse> getById(UUID id, UUID municipalityId) {
        log.info("Getting movement ID: {} for municipality: {}", id, municipalityId);
        return assetMovementRepository.findActiveByIdAndMunicipalityId(id, municipalityId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)));
    }

    @Transactional
    public Mono<AssetMovementResponse> update(UUID id, UUID municipalityId, AssetMovementRequest request) {
        log.info("Updating movement ID: {} for municipality: {}", id, municipalityId);

        return assetMovementRepository.findActiveByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Active asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (request.getMovementNumber() != null && 
                        !request.getMovementNumber().equals(existing.getMovementNumber())) {
                        return Mono.error(new IllegalStateException(
                                "Movement number cannot be changed. Current: " + existing.getMovementNumber() + 
                                ", Attempted: " + request.getMovementNumber()));
                    }
                    
                    UUID assetId = request.getAssetId() != null ? request.getAssetId() : existing.getAssetId();
                    String movementType = request.getMovementType() != null ? request.getMovementType() : existing.getMovementType();
                    String movementSubtype = request.getMovementSubtype() != null ? request.getMovementSubtype() : existing.getMovementSubtype();
                    UUID originResponsibleId = request.getOriginResponsibleId() != null ? request.getOriginResponsibleId() : existing.getOriginResponsibleId();
                    UUID destinationResponsibleId = request.getDestinationResponsibleId() != null ? request.getDestinationResponsibleId() : existing.getDestinationResponsibleId();
                    UUID originAreaId = request.getOriginAreaId() != null ? request.getOriginAreaId() : existing.getOriginAreaId();
                    UUID destinationAreaId = request.getDestinationAreaId() != null ? request.getDestinationAreaId() : existing.getDestinationAreaId();
                    UUID originLocationId = request.getOriginLocationId() != null ? request.getOriginLocationId() : existing.getOriginLocationId();
                    UUID destinationLocationId = request.getDestinationLocationId() != null ? request.getDestinationLocationId() : existing.getDestinationLocationId();
                    LocalDateTime requestDate = request.getRequestDate() != null ? request.getRequestDate() : existing.getRequestDate();
                    LocalDateTime approvalDate = request.getApprovalDate() != null ? request.getApprovalDate() : existing.getApprovalDate();
                    LocalDateTime executionDate = request.getExecutionDate() != null ? request.getExecutionDate() : existing.getExecutionDate();
                    LocalDateTime receptionDate = request.getReceptionDate() != null ? request.getReceptionDate() : existing.getReceptionDate();
                    String movementStatus = request.getMovementStatus() != null ? request.getMovementStatus() : existing.getMovementStatus();
                    Boolean requiresApproval = request.getRequiresApproval() != null ? request.getRequiresApproval() : existing.getRequiresApproval();
                    UUID approvedBy = request.getApprovedBy() != null ? request.getApprovedBy() : existing.getApprovedBy();
                    String reason = request.getReason() != null ? request.getReason() : existing.getReason();
                    String observations = request.getObservations() != null ? request.getObservations() : existing.getObservations();
                    String specialConditions = request.getSpecialConditions() != null ? request.getSpecialConditions() : existing.getSpecialConditions();
                    String supportingDocumentNumber = request.getSupportingDocumentNumber() != null ? request.getSupportingDocumentNumber() : existing.getSupportingDocumentNumber();
                    String supportingDocumentType = request.getSupportingDocumentType() != null ? request.getSupportingDocumentType() : existing.getSupportingDocumentType();
                    
                    String attachedDocuments = request.getAttachedDocuments() != null 
                            ? validateAndNormalizeAttachedDocuments(request.getAttachedDocuments())
                            : existing.getAttachedDocuments();
                    UUID requestingUser = request.getRequestingUser() != null ? request.getRequestingUser() : existing.getRequestingUser();
                    UUID executingUser = request.getExecutingUser() != null ? request.getExecutingUser() : existing.getExecutingUser();
                    
                    DatabaseClient.GenericExecuteSpec sqlSpec = databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "asset_id = :assetId, " +
                            "movement_type = :movementType, " +
                            "movement_subtype = :movementSubtype, " +
                            "origin_responsible_id = :originResponsibleId, " +
                            "destination_responsible_id = :destinationResponsibleId, " +
                            "origin_area_id = :originAreaId, " +
                            "destination_area_id = :destinationAreaId, " +
                            "origin_location_id = :originLocationId, " +
                            "destination_location_id = :destinationLocationId, " +
                            "request_date = :requestDate, " +
                            "approval_date = :approvalDate, " +
                            "execution_date = :executionDate, " +
                            "reception_date = :receptionDate, " +
                            "movement_status = :movementStatus, " +
                            "requires_approval = :requiresApproval, " +
                            "approved_by = :approvedBy, " +
                            "reason = :reason, " +
                            "observations = :observations, " +
                            "special_conditions = :specialConditions, " +
                            "supporting_document_number = :supportingDocumentNumber, " +
                            "supporting_document_type = :supportingDocumentType, " +
                            "attached_documents = :attachedDocuments::jsonb, " +
                            "requesting_user = :requestingUser, " +
                            "executing_user = :executingUser, " +
                            "updated_at = CURRENT_TIMESTAMP " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("assetId", assetId)
                    .bind("movementType", movementType);
                    
                    sqlSpec = (movementSubtype != null) ? sqlSpec.bind("movementSubtype", movementSubtype) : sqlSpec.bindNull("movementSubtype", String.class);
                    sqlSpec = (originResponsibleId != null) ? sqlSpec.bind("originResponsibleId", originResponsibleId) : sqlSpec.bindNull("originResponsibleId", UUID.class);
                    sqlSpec = (destinationResponsibleId != null) ? sqlSpec.bind("destinationResponsibleId", destinationResponsibleId) : sqlSpec.bindNull("destinationResponsibleId", UUID.class);
                    sqlSpec = (originAreaId != null) ? sqlSpec.bind("originAreaId", originAreaId) : sqlSpec.bindNull("originAreaId", UUID.class);
                    sqlSpec = (destinationAreaId != null) ? sqlSpec.bind("destinationAreaId", destinationAreaId) : sqlSpec.bindNull("destinationAreaId", UUID.class);
                    sqlSpec = (originLocationId != null) ? sqlSpec.bind("originLocationId", originLocationId) : sqlSpec.bindNull("originLocationId", UUID.class);
                    sqlSpec = (destinationLocationId != null) ? sqlSpec.bind("destinationLocationId", destinationLocationId) : sqlSpec.bindNull("destinationLocationId", UUID.class);
                    sqlSpec = sqlSpec.bind("requestDate", requestDate);
                    sqlSpec = (approvalDate != null) ? sqlSpec.bind("approvalDate", approvalDate) : sqlSpec.bindNull("approvalDate", LocalDateTime.class);
                    sqlSpec = (executionDate != null) ? sqlSpec.bind("executionDate", executionDate) : sqlSpec.bindNull("executionDate", LocalDateTime.class);
                    sqlSpec = (receptionDate != null) ? sqlSpec.bind("receptionDate", receptionDate) : sqlSpec.bindNull("receptionDate", LocalDateTime.class);
                    sqlSpec = sqlSpec.bind("movementStatus", movementStatus);
                    sqlSpec = sqlSpec.bind("requiresApproval", requiresApproval);
                    sqlSpec = (approvedBy != null) ? sqlSpec.bind("approvedBy", approvedBy) : sqlSpec.bindNull("approvedBy", UUID.class);
                    sqlSpec = sqlSpec.bind("reason", reason);
                    sqlSpec = (observations != null) ? sqlSpec.bind("observations", observations) : sqlSpec.bindNull("observations", String.class);
                    sqlSpec = (specialConditions != null) ? sqlSpec.bind("specialConditions", specialConditions) : sqlSpec.bindNull("specialConditions", String.class);
                    sqlSpec = (supportingDocumentNumber != null) ? sqlSpec.bind("supportingDocumentNumber", supportingDocumentNumber) : sqlSpec.bindNull("supportingDocumentNumber", String.class);
                    sqlSpec = (supportingDocumentType != null) ? sqlSpec.bind("supportingDocumentType", supportingDocumentType) : sqlSpec.bindNull("supportingDocumentType", String.class);
                    sqlSpec = sqlSpec.bind("attachedDocuments", attachedDocuments); 
                    sqlSpec = sqlSpec.bind("requestingUser", requestingUser);
                    sqlSpec = (executingUser != null) ? sqlSpec.bind("executingUser", executingUser) : sqlSpec.bindNull("executingUser", UUID.class);
                    sqlSpec = sqlSpec.bind("id", id);
                    sqlSpec = sqlSpec.bind("municipalityId", municipalityId);
                    
                    return sqlSpec
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to update movement. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findActiveByIdAndMunicipalityId(id, municipalityId);
                    })
                    .doOnError(error -> log.error("Error updating asset movement: {}", error.getMessage(), error));
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Asset movement updated successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> delete(UUID id, UUID municipalityId, UUID deletedBy) {
        log.info("Soft deleting movement ID: {} by user: {} for municipality: {}", id, deletedBy, municipalityId);

        return assetMovementRepository.findActiveByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Active asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    return assetMovementRepository.softDeleteById(id, municipalityId, deletedBy)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == 0) {
                                    return Mono.error(new IllegalStateException(
                                            "Failed to soft delete movement. Movement may have been already deleted."));
                                }
                                return assetMovementRepository.findByIdAndMunicipalityIdIncludingDeleted(id, municipalityId);
                            });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Asset movement soft deleted successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> restore(UUID id, UUID municipalityId, UUID restoredBy) {
        log.info("Restoring movement ID: {} by user: {} for municipality: {}", id, restoredBy, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityIdIncludingDeleted(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (existing.getActive() != null && existing.getActive()) {
                        return Mono.error(new IllegalStateException(
                                "Asset movement with ID " + id + " is already active"));
                    }

                    return assetMovementRepository.restoreById(id, municipalityId, restoredBy)
                            .flatMap(rowsUpdated -> {
                                if (rowsUpdated == 0) {
                                    return Mono.error(new IllegalStateException(
                                            "Failed to restore movement. Movement may have been already restored or is not deleted."));
                                }
                                return assetMovementRepository.findByIdAndMunicipalityIdIncludingDeleted(id, municipalityId);
                            });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Asset movement restored successfully with ID: {}", m.getId()));
    }

    public Flux<AssetMovementResponse> getDeletedByMunicipality(UUID municipalityId) {
        log.info("Getting deleted movements for municipality: {}", municipalityId);
        return assetMovementRepository.findDeletedByMunicipalityId(municipalityId)
                .map(this::mapToResponse);
    }

    public Flux<AssetMovementResponse> getByAsset(UUID assetId, UUID municipalityId) {
        log.info("Getting movements for assetId: {} and municipality: {}", assetId, municipalityId);
        return assetMovementRepository.findByAssetIdAndMunicipalityIdOrderByRequestDateDesc(assetId, municipalityId)
                .map(this::mapToResponse);
    }

    public Flux<AssetMovementResponse> getByMovementType(String movementType, UUID municipalityId) {
        log.info("Getting movements of type: {} for municipality: {}", movementType, municipalityId);
        return assetMovementRepository.findByMovementTypeAndMunicipalityIdOrderByRequestDateDesc(movementType, municipalityId)
                .map(this::mapToResponse);
    }

    public Flux<AssetMovementResponse> getByStatus(String status, UUID municipalityId) {
        log.info("Getting movements with status: {} for municipality: {}", status, municipalityId);
        return assetMovementRepository.findByMovementStatusAndMunicipalityIdOrderByRequestDateDesc(status, municipalityId)
                .map(this::mapToResponse);
    }

    public Flux<AssetMovementResponse> getPendingApproval(UUID municipalityId) {
        log.info("Getting pending approval movements for municipality: {}", municipalityId);
        return assetMovementRepository.findPendingApprovalMovements(municipalityId)
                .map(this::mapToResponse);
    }

    @Transactional
    public Mono<AssetMovementResponse> approve(UUID id, UUID municipalityId, UUID approvedBy) {
        log.info("Approving movement ID: {} by user: {} for municipality: {}", id, approvedBy, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (!"REQUESTED".equals(existing.getMovementStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Only movements with status REQUESTED can be approved. Current status: " + existing.getMovementStatus()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    return databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "movement_status = :movementStatus, " +
                            "approval_date = :approvalDate, " +
                            "approved_by = :approvedBy, " +
                            "updated_at = :updatedAt " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("movementStatus", "APPROVED")
                    .bind("approvalDate", now)
                    .bind("approvedBy", approvedBy)
                    .bind("updatedAt", now)
                    .bind("id", id)
                    .bind("municipalityId", municipalityId)
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to approve movement. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId);
                    });
                })
                
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Movement approved successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> reject(UUID id, UUID municipalityId, UUID approvedBy, String rejectionReason) {
        log.info("Rejecting movement ID: {} by user: {} for municipality: {}", id, approvedBy, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (!"REQUESTED".equals(existing.getMovementStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Only movements with status REQUESTED can be rejected. Current status: " + existing.getMovementStatus()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    String updatedObservations = existing.getObservations() != null ? existing.getObservations() : "";
                    if (rejectionReason != null && !rejectionReason.isEmpty()) {
                        updatedObservations = updatedObservations + "\n[REJECTION REASON]: " + rejectionReason;
                    }
                    
                    return databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "movement_status = :movementStatus, " +
                            "approval_date = :approvalDate, " +
                            "approved_by = :approvedBy, " +
                            "observations = :observations, " +
                            "updated_at = :updatedAt " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("movementStatus", "REJECTED")
                    .bind("approvalDate", now)
                    .bind("approvedBy", approvedBy)
                    .bind("observations", updatedObservations)
                    .bind("updatedAt", now)
                    .bind("id", id)
                    .bind("municipalityId", municipalityId)
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to reject movement. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId);
                    });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Movement rejected successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> markInProcess(UUID id, UUID municipalityId, UUID executingUser) {
        log.info("Marking movement ID: {} as in process for municipality: {}", id, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (!"APPROVED".equals(existing.getMovementStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Only movements with status APPROVED can be marked as in process. Current status: " + existing.getMovementStatus()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    return databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "movement_status = :movementStatus, " +
                            "execution_date = :executionDate, " +
                            "executing_user = :executingUser, " +
                            "updated_at = :updatedAt " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("movementStatus", "IN_PROCESS")
                    .bind("executionDate", now)
                    .bind("executingUser", executingUser)
                    .bind("updatedAt", now)
                    .bind("id", id)
                    .bind("municipalityId", municipalityId)
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to mark movement as in process. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId);
                    });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Movement marked as in process successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> complete(UUID id, UUID municipalityId) {
        log.info("Completing movement ID: {} for municipality: {}", id, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if (!"IN_PROCESS".equals(existing.getMovementStatus()) && !"APPROVED".equals(existing.getMovementStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Only movements with status IN_PROCESS or APPROVED can be completed. Current status: " + existing.getMovementStatus()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime executionDate = existing.getExecutionDate() != null ? existing.getExecutionDate() : now;
                    
                    return databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "movement_status = :movementStatus, " +
                            "reception_date = :receptionDate, " +
                            "execution_date = :executionDate, " +
                            "updated_at = :updatedAt " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("movementStatus", "COMPLETED")
                    .bind("receptionDate", now)
                    .bind("executionDate", executionDate)
                    .bind("updatedAt", now)
                    .bind("id", id)
                    .bind("municipalityId", municipalityId)
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to complete movement. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId);
                    });
                })
                .flatMap(completedMovement -> {
                    AssetUpdateRequest assetUpdate = AssetUpdateRequest.builder()
                            .assetStatus(mapMovementTypeToAssetStatus(completedMovement.getMovementType()))
                            .currentResponsibleId(completedMovement.getDestinationResponsibleId())
                            .currentAreaId(completedMovement.getDestinationAreaId())
                            .currentLocationId(completedMovement.getDestinationLocationId())
                            .observations("Movement completed: " + completedMovement.getMovementNumber())
                            .build();

                    return assetServiceClient.updateAssetOnCompletion(
                            completedMovement.getAssetId(),
                            completedMovement.getMunicipalityId(),
                            assetUpdate
                    ).thenReturn(completedMovement)
                    .onErrorResume(error -> {
                        log.warn("Failed to update asset status on completion, but movement is completed: {}", 
                                completedMovement.getId(), error);
                        return Mono.just(completedMovement);
                    });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Movement completed successfully with ID: {}", m.getId()));
    }

    @Transactional
    public Mono<AssetMovementResponse> cancel(UUID id, UUID municipalityId, String cancellationReason) {
        log.info("Cancelling movement ID: {} for municipality: {}", id, municipalityId);

        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Asset movement with ID " + id + " not found for municipality " + municipalityId)))
                .flatMap(existing -> {
                    if ("COMPLETED".equals(existing.getMovementStatus()) || "CANCELLED".equals(existing.getMovementStatus())) {
                        return Mono.error(new IllegalStateException(
                                "Cannot cancel a movement with status: " + existing.getMovementStatus()));
                    }

                    LocalDateTime now = LocalDateTime.now();
                    String updatedObservations = existing.getObservations() != null ? existing.getObservations() : "";
                    if (cancellationReason != null && !cancellationReason.isEmpty()) {
                        updatedObservations = updatedObservations + "\n[CANCELLATION REASON]: " + cancellationReason;
                    }
                    
                    return databaseClient.sql(
                            "UPDATE asset_movements SET " +
                            "movement_status = :movementStatus, " +
                            "observations = :observations, " +
                            "updated_at = :updatedAt " +
                            "WHERE id = :id AND municipality_id = :municipalityId AND active = true"
                    )
                    .bind("movementStatus", "CANCELLED")
                    .bind("observations", updatedObservations)
                    .bind("updatedAt", now)
                    .bind("id", id)
                    .bind("municipalityId", municipalityId)
                    .fetch()
                    .rowsUpdated()
                    .flatMap(rowsUpdated -> {
                        if (rowsUpdated == 0) {
                            return Mono.error(new IllegalStateException(
                                    "Failed to cancel movement. Movement may have been deleted or municipality mismatch."));
                        }
                        return assetMovementRepository.findByIdAndMunicipalityId(id, municipalityId);
                    });
                })
                .map(this::mapToResponse)
                .doOnSuccess(m -> log.info("Movement cancelled successfully with ID: {}", m.getId()));
    }

    public Flux<AssetMovementResponse> getByDestinationResponsible(UUID destinationResponsibleId, UUID municipalityId) {
        log.info("Getting movements for destination responsible: {} and municipality: {}", destinationResponsibleId, municipalityId);
        return assetMovementRepository.findByDestinationResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(destinationResponsibleId, municipalityId)
                .map(this::mapToResponse);
    }

    public Flux<AssetMovementResponse> getByOriginResponsible(UUID originResponsibleId, UUID municipalityId) {
        log.info("Getting movements for origin responsible: {} and municipality: {}", originResponsibleId, municipalityId);
        return assetMovementRepository.findByOriginResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(originResponsibleId, municipalityId)
                .map(this::mapToResponse);
    }

    public Mono<Long> countByMunicipality(UUID municipalityId) {
        log.info("Counting movements for municipality: {}", municipalityId);
        return assetMovementRepository.countByMunicipalityId(municipalityId);
    }

    private String mapMovementTypeToAssetStatus(String movementType) {
        if (movementType == null) {
            return "IN_USE";
        }

        return switch (movementType) {
            case "INITIAL_ASSIGNMENT", "REASSIGNMENT", "AREA_TRANSFER", "EXTERNAL_TRANSFER", "LOAN" -> "IN_USE";
            case "RETURN" -> "DISPONIBLE";
            case "MAINTENANCE" -> "EN_MANTENIMIENTO";
            case "REPAIR" -> "EN_REPARACION";
            case "TEMPORARY_DISPOSAL", "DESTROY" -> "DE_BAJA";
            default -> "IN_USE";
        };
    }

    private AssetMovementResponse mapToResponse(AssetMovement movement) {
        String attachedDocuments = movement.getAttachedDocuments();
        if (attachedDocuments == null || attachedDocuments.trim().isEmpty()) {
            attachedDocuments = "[]";
        } else {
            try {
                Object parsed = objectMapper.readValue(attachedDocuments, Object.class);
                attachedDocuments = objectMapper.writeValueAsString(parsed);
            } catch (JsonProcessingException e) {
                log.warn("Could not parse attachedDocuments as JSON for movement {}: {}", 
                        movement.getId(), e.getMessage());
                if (!attachedDocuments.trim().startsWith("[")) {
                    attachedDocuments = "[]";
                }
            }
        }
        
        return AssetMovementResponse.builder()
                .id(movement.getId())
                .municipalityId(movement.getMunicipalityId())
                .movementNumber(movement.getMovementNumber())
                .assetId(movement.getAssetId())
                .movementType(movement.getMovementType())
                .movementSubtype(movement.getMovementSubtype())
                .originResponsibleId(movement.getOriginResponsibleId())
                .destinationResponsibleId(movement.getDestinationResponsibleId())
                .originAreaId(movement.getOriginAreaId())
                .destinationAreaId(movement.getDestinationAreaId())
                .originLocationId(movement.getOriginLocationId())
                .destinationLocationId(movement.getDestinationLocationId())
                .requestDate(movement.getRequestDate())
                .approvalDate(movement.getApprovalDate())
                .executionDate(movement.getExecutionDate())
                .receptionDate(movement.getReceptionDate())
                .movementStatus(movement.getMovementStatus())
                .requiresApproval(movement.getRequiresApproval())
                .approvedBy(movement.getApprovedBy())
                .reason(movement.getReason())
                .observations(movement.getObservations())
                .specialConditions(movement.getSpecialConditions())
                .supportingDocumentNumber(movement.getSupportingDocumentNumber())
                .supportingDocumentType(movement.getSupportingDocumentType())
                .attachedDocuments(attachedDocuments)
                .requestingUser(movement.getRequestingUser())
                .executingUser(movement.getExecutingUser())
                .createdAt(movement.getCreatedAt())
                .updatedAt(movement.getUpdatedAt())
                .active(movement.getActive())
                .deletedBy(movement.getDeletedBy())
                .deletedAt(movement.getDeletedAt())
                .restoredBy(movement.getRestoredBy())
                .restoredAt(movement.getRestoredAt())
                .build();
    }
}
