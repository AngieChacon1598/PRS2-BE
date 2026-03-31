package pe.edu.vallegrande.movementservice.application.ports.output;

import pe.edu.vallegrande.movementservice.domain.model.AssetMovement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetMovementRepositoryPort {
    Mono<AssetMovement> save(AssetMovement assetMovement);
    Mono<AssetMovement> findById(UUID id);
    Mono<AssetMovement> findActiveByIdAndMunicipalityId(UUID id, UUID municipalityId);
    Flux<AssetMovement> findByMunicipalityIdOrderByRequestDateDesc(UUID municipalityId);
    Mono<AssetMovement> findByMovementNumberAndMunicipalityId(String movementNumber, UUID municipalityId);
    Mono<String> findLastMovementNumberByMunicipalityId(UUID municipalityId);
    Flux<AssetMovement> findByAssetIdAndMunicipalityIdOrderByRequestDateDesc(UUID assetId, UUID municipalityId);
    Flux<AssetMovement> findByMovementTypeAndMunicipalityIdOrderByRequestDateDesc(String movementType, UUID municipalityId);
    Flux<AssetMovement> findByMovementStatusAndMunicipalityIdOrderByRequestDateDesc(String movementStatus, UUID municipalityId);
    Flux<AssetMovement> findPendingApprovalMovements(UUID municipalityId);
    Flux<AssetMovement> findByDestinationResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(UUID destinationResponsibleId, UUID municipalityId);
    Flux<AssetMovement> findByOriginResponsibleIdAndMunicipalityIdOrderByRequestDateDesc(UUID originResponsibleId, UUID municipalityId);
    Mono<Long> countByMunicipalityId(UUID municipalityId);
    Flux<AssetMovement> findDeletedByMunicipalityId(UUID municipalityId);
    Mono<AssetMovement> findByIdAndMunicipalityId(UUID id, UUID municipalityId);
    Mono<AssetMovement> findByIdAndMunicipalityIdIncludingDeleted(UUID id, UUID municipalityId);
    Mono<Integer> softDeleteById(UUID id, UUID municipalityId, UUID deletedBy);
    Mono<Integer> restoreById(UUID id, UUID municipalityId, UUID restoredBy);
}
