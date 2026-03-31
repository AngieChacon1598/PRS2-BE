package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetDisposalDetailUseCase {

    Mono<AssetDisposalDetailResponse> create(AssetDisposalDetailRequest request);

    Mono<AssetDisposalDetailResponse> getById(UUID id);

    Flux<AssetDisposalDetailResponse> getByDisposalId(UUID disposalId);

    Flux<AssetDisposalDetailResponse> getByAssetId(UUID assetId);

    Mono<AssetDisposalDetailResponse> addTechnicalOpinion(UUID id, TechnicalOpinionRequest request);

    Mono<AssetDisposalDetailResponse> executeRemoval(UUID id, ExecuteRemovalRequest request);

    Mono<Void> delete(UUID id);

    Flux<UUID> findActiveAssetIds();
}
