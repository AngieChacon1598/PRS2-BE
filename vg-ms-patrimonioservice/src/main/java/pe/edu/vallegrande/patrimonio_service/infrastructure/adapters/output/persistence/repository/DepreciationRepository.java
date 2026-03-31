package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.Depreciation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DepreciationRepository extends ReactiveCrudRepository<Depreciation, UUID> {
    
    Flux<Depreciation> findByAssetId(UUID assetId);
    
    Flux<Depreciation> findByFiscalYear(Integer fiscalYear);
    
    Mono<Depreciation> findByAssetIdAndFiscalYearAndCalculationMonth(
        UUID assetId, 
        Integer fiscalYear, 
        Integer calculationMonth
    );
    
    Flux<Depreciation> findByCalculationStatus(String calculationStatus);
}
