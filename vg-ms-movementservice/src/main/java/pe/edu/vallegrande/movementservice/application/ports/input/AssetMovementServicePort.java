package pe.edu.vallegrande.movementservice.application.ports.input;

import pe.edu.vallegrande.movementservice.application.dto.AssetMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetMovementServicePort {
    Mono<AssetMovementResponse> create(AssetMovementRequest request);
    Flux<AssetMovementResponse> getAllByMunicipality(UUID municipalityId);
    Mono<AssetMovementResponse> getById(UUID id, UUID municipalityId);
    Mono<AssetMovementResponse> update(UUID id, UUID municipalityId, AssetMovementRequest request);
    Mono<AssetMovementResponse> delete(UUID id, UUID municipalityId, UUID deletedBy);
    Flux<AssetMovementResponse> getByAsset(UUID assetId, UUID municipalityId);
    Flux<AssetMovementResponse> getByMovementType(String movementType, UUID municipalityId);
    Flux<AssetMovementResponse> getByStatus(String status, UUID municipalityId);
    Flux<AssetMovementResponse> getPendingApproval(UUID municipalityId);
    Mono<AssetMovementResponse> approve(UUID id, UUID municipalityId, UUID approvedBy);
    Mono<AssetMovementResponse> reject(UUID id, UUID municipalityId, UUID approvedBy, String rejectionReason);
    Mono<AssetMovementResponse> markInProcess(UUID id, UUID municipalityId, UUID executingUser);
    Mono<AssetMovementResponse> complete(UUID id, UUID municipalityId);
    Mono<AssetMovementResponse> cancel(UUID id, UUID municipalityId, String cancellationReason);
    Flux<AssetMovementResponse> getByDestinationResponsible(UUID destinationResponsibleId, UUID municipalityId);
    Flux<AssetMovementResponse> getByOriginResponsible(UUID originResponsibleId, UUID municipalityId);
    Mono<Long> countByMunicipality(UUID municipalityId);
    Flux<AssetMovementResponse> getDeletedByMunicipality(UUID municipalityId);
    Mono<AssetMovementResponse> restore(UUID id, UUID municipalityId, UUID restoredBy);
}
