package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.DepreciationPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.DepreciationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class DepreciationPersistenceAdapter implements DepreciationPersistencePort {

    private final DepreciationRepository repository;

    public DepreciationPersistenceAdapter(DepreciationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Depreciation> save(Depreciation depreciation) {
        return repository.save(depreciation);
    }

    @Override
    public Mono<Depreciation> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<Depreciation> findAll() {
        return repository.findAll();
    }

    @Override
    public Flux<Depreciation> findByAssetId(UUID assetId) {
        return repository.findByAssetId(assetId);
    }

    @Override
    public Flux<Depreciation> findByFiscalYear(Integer fiscalYear) {
        return repository.findByFiscalYear(fiscalYear);
    }

    @Override
    public Mono<Depreciation> findByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth) {
        return repository.findByAssetIdAndFiscalYearAndCalculationMonth(assetId, fiscalYear, calculationMonth);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth) {
        return repository.findByAssetId(assetId)
                .flatMap(repository::delete)
                .then();
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }
}
