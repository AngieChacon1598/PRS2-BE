package pe.edu.vallegrande.patrimonio_service.application.ports.output;

import java.util.UUID;

import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DepreciationPersistencePort {

    Mono<Depreciation> save(Depreciation depreciation);

    Mono<Depreciation> findById(UUID id);

    Flux<Depreciation> findAll();

    Flux<Depreciation> findByAssetId(UUID assetId);

    Flux<Depreciation> findByFiscalYear(Integer fiscalYear);

    Mono<Depreciation> findByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth);

    Mono<Void> deleteById(UUID id);
    
    Mono<Void> deleteByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth);

    Mono<Boolean> existsById(UUID id);
}
