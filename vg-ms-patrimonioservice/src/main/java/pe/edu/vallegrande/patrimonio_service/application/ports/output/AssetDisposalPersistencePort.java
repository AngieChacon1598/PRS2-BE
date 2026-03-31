package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetDisposalPersistencePort {

    Mono<AssetDisposal> save(AssetDisposal assetDisposal);

    Mono<AssetDisposal> findById(UUID id);

    Flux<AssetDisposal> findAll();

    Flux<AssetDisposal> findByFileStatus(String fileStatus);

    Mono<AssetDisposal> findByFileNumber(String fileNumber);

    Flux<AssetDisposal> findByRequestedBy(UUID requestedBy);

    Mono<Void> deleteById(UUID id);

    Mono<Boolean> existsById(UUID id);

    Mono<Boolean> existsByFileNumber(String fileNumber);
}
