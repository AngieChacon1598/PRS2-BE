package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetDisposalDetailPersistencePort {

    Mono<AssetDisposalDetail> save(AssetDisposalDetail assetDisposalDetail);

    Mono<AssetDisposalDetail> findById(UUID id);

    Flux<AssetDisposalDetail> findAll();

    Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId);

    Flux<AssetDisposalDetail> findByAssetId(UUID assetId);

    Mono<Void> deleteById(UUID id);

    Mono<Boolean> existsById(UUID id);

    Mono<Boolean> existsByDisposalIdAndAssetId(UUID disposalId, UUID assetId);

    Flux<UUID> findActiveAssetIds();
}
