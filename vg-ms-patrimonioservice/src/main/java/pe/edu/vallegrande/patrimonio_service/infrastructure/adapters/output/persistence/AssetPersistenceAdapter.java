package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetPersistenceAdapter implements AssetPersistencePort {

    private final AssetRepository repository;

    public AssetPersistenceAdapter(AssetRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Asset> save(Asset asset) {
        return repository.save(asset);
    }

    @Override
    public Mono<Asset> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<Asset> findAll() {
        return repository.findAll();
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
    public Flux<Asset> findByAssetStatus(String status) {
        return repository.findByAssetStatus(status);
    }

    @Override
    public Mono<Asset> findByAssetCode(String assetCode) {
        return repository.findByAssetCode(assetCode);
    }

    @Override
    public Flux<Asset> findByCurrentLocationId(UUID locationId) {
        return repository.findByCurrentLocationId(locationId);
    }

    @Override
    public Flux<Asset> findByCurrentResponsibleId(UUID responsibleId) {
        return repository.findByCurrentResponsibleId(responsibleId);
    }

    @Override
    public Mono<Long> countByAssetStatus(String status) {
        return repository.countByAssetStatus(status);
    }
}
