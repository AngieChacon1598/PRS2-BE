package pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.movementservice.domain.model.AssetMovement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AssetMovementRepository extends ReactiveCrudRepository<AssetMovement, UUID> {

    @Query("SELECT id, municipality_id, movement_number, asset_id, movement_type, movement_subtype, " +
           "origin_responsible_id, destination_responsible_id, origin_area_id, destination_area_id, " +
           "origin_location_id, destination_location_id, request_date, approval_date, execution_date, " +
           "reception_date, movement_status, requires_approval, approved_by, reason, observations, " +
           "special_conditions, supporting_document_number, supporting_document_type, " +
           "attached_documents::text as attached_documents, requesting_user, executing_user, " +
           "created_at, updated_at, active, deleted_by, deleted_at, restored_by, restored_at " +
           "FROM asset_movements WHERE municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByMunicipalityIdOrderByRequestDateDesc(UUID municipalityId);

    Mono<AssetMovement> findByIdAndMunicipalityId(UUID id, UUID municipalityId);

    @Query("SELECT id, municipality_id, movement_number, asset_id, movement_type, movement_subtype, " +
           "origin_responsible_id, destination_responsible_id, origin_area_id, destination_area_id, " +
           "origin_location_id, destination_location_id, request_date, approval_date, execution_date, " +
           "reception_date, movement_status, requires_approval, approved_by, reason, observations, " +
           "special_conditions, supporting_document_number, supporting_document_type, " +
           "attached_documents::text as attached_documents, requesting_user, executing_user, " +
           "created_at, updated_at, active, deleted_by, deleted_at, restored_by, restored_at " +
           "FROM asset_movements WHERE id = :id AND municipality_id = :municipalityId AND active = true")
    Mono<AssetMovement> findActiveByIdAndMunicipalityId(UUID id, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE asset_id = :assetId AND municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByAssetIdAndMunicipalityIdOrderByRequestDateDesc(UUID assetId, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE movement_type = :movementType AND municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByMovementTypeAndMunicipalityIdOrderByRequestDateDesc(String movementType, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE movement_status = :movementStatus AND municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByMovementStatusAndMunicipalityIdOrderByRequestDateDesc(String movementStatus, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE destination_responsible_id = :destinationResponsibleId AND municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByDestinationResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(UUID destinationResponsibleId, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE origin_responsible_id = :originResponsibleId AND municipality_id = :municipalityId AND active = true ORDER BY request_date DESC")
    Flux<AssetMovement> findByOriginResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(UUID originResponsibleId, UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE municipality_id = :municipalityId AND movement_status = 'REQUESTED' AND requires_approval = true AND active = true ORDER BY request_date ASC")
    Flux<AssetMovement> findPendingApprovalMovements(UUID municipalityId);

    @Query("SELECT id, municipality_id, movement_number, asset_id, movement_type, movement_subtype, " +
           "origin_responsible_id, destination_responsible_id, origin_area_id, destination_area_id, " +
           "origin_location_id, destination_location_id, request_date, approval_date, execution_date, " +
           "reception_date, movement_status, requires_approval, approved_by, reason, observations, " +
           "special_conditions, supporting_document_number, supporting_document_type, " +
           "attached_documents::text as attached_documents, requesting_user, executing_user, " +
           "created_at, updated_at, active, deleted_by, deleted_at, restored_by, restored_at " +
           "FROM asset_movements WHERE movement_number = :movementNumber AND municipality_id = :municipalityId AND active = true")
    Mono<AssetMovement> findByMovementNumberAndMunicipalityId(String movementNumber, UUID municipalityId);

    @Query("SELECT movement_number FROM asset_movements WHERE municipality_id = :municipalityId AND movement_number ~ '^MV-\\d+$' ORDER BY CAST(SUBSTRING(movement_number FROM 'MV-(\\d+)') AS INTEGER) DESC LIMIT 1")
    Mono<String> findLastMovementNumberByMunicipalityId(UUID municipalityId);

    @Query("SELECT COUNT(*) FROM asset_movements WHERE municipality_id = :municipalityId AND active = true")
    Mono<Long> countByMunicipalityId(UUID municipalityId);

    @Query("SELECT * FROM asset_movements WHERE municipality_id = :municipalityId AND active = false ORDER BY deleted_at DESC")
    Flux<AssetMovement> findDeletedByMunicipalityId(UUID municipalityId);

    @Query("SELECT id, municipality_id, movement_number, asset_id, movement_type, movement_subtype, " +
           "origin_responsible_id, destination_responsible_id, origin_area_id, destination_area_id, " +
           "origin_location_id, destination_location_id, request_date, approval_date, execution_date, " +
           "reception_date, movement_status, requires_approval, approved_by, reason, observations, " +
           "special_conditions, supporting_document_number, supporting_document_type, " +
           "attached_documents::text as attached_documents, requesting_user, executing_user, " +
           "created_at, updated_at, active, deleted_by, deleted_at, restored_by, restored_at " +
           "FROM asset_movements WHERE id = :id AND municipality_id = :municipalityId")
    Mono<AssetMovement> findByIdAndMunicipalityIdIncludingDeleted(UUID id, UUID municipalityId);

    @Modifying
    @Query("UPDATE asset_movements SET active = false, deleted_by = :deletedBy, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP, restored_by = NULL, restored_at = NULL WHERE id = :id AND municipality_id = :municipalityId AND active = true")
    Mono<Integer> softDeleteById(UUID id, UUID municipalityId, UUID deletedBy);

    @Modifying
    @Query("UPDATE asset_movements SET active = true, restored_by = :restoredBy, restored_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = :id AND municipality_id = :municipalityId AND active = false")
    Mono<Integer> restoreById(UUID id, UUID municipalityId, UUID restoredBy);
}
