package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AssetDisposalPersistenceAdapter implements AssetDisposalPersistencePort {

    private final AssetDisposalRepository repository;

    public AssetDisposalPersistenceAdapter(AssetDisposalRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AssetDisposal> save(AssetDisposal assetDisposal) {
        return repository.save(assetDisposal);
    }

    @Override
    public Mono<AssetDisposal> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<AssetDisposal> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<AssetDisposal> findByFileStatus(String fileStatus) {
        return repository.findByFileStatus(fileStatus);
    }

    @Override
    public Mono<AssetDisposal> findByFileNumber(String fileNumber) {
        return repository.findByFileNumber(fileNumber);
    }

    @Override
    public Flux<AssetDisposal> findByRequestedBy(UUID requestedBy) {
        return repository.findByRequestedBy(requestedBy);
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
    public Mono<Boolean> existsByFileNumber(String fileNumber) {
        return repository.findByFileNumber(fileNumber).hasElement();
    }
}
