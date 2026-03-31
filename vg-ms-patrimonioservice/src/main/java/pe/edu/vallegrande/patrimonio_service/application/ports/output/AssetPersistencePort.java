package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import java.util.UUID;

import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetPersistencePort {

    Mono<Asset> save(Asset asset);

    Mono<Asset> findById(UUID id);

    Flux<Asset> findAll();

    Mono<Void> deleteById(UUID id);

    Mono<Boolean> existsById(UUID id);

    Flux<Asset> findByAssetStatus(String status);

    Mono<Asset> findByAssetCode(String assetCode);

    Flux<Asset> findByCurrentLocationId(UUID locationId);

    Flux<Asset> findByCurrentResponsibleId(UUID responsibleId);

    Mono<Long> countByAssetStatus(String status);
}
