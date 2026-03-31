package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetDisposalDetailPersistenceAdapter implements AssetDisposalDetailPersistencePort {

    private final AssetDisposalDetailRepository repository;

    public AssetDisposalDetailPersistenceAdapter(AssetDisposalDetailRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AssetDisposalDetail> save(AssetDisposalDetail assetDisposalDetail) {
        return repository.save(assetDisposalDetail);
    }

    @Override
    public Mono<AssetDisposalDetail> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<AssetDisposalDetail> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId) {
        return repository.findByDisposalId(disposalId);
    }

    @Override
    public Flux<AssetDisposalDetail> findByAssetId(UUID assetId) {
        return repository.findByAssetId(assetId);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsByDisposalIdAndAssetId(UUID disposalId, UUID assetId) {
        return repository.findByDisposalId(disposalId)
                .filter(detail -> detail.getAssetId().equals(assetId))
                .hasElements();
    }

    @Override
    public Flux<UUID> findActiveAssetIds() {
        return repository.findActiveAssetIds();
    }
}
