package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DepreciationServicePort {

    Mono<DepreciationResponse> create(DepreciationRequest request);
    
    Mono<DepreciationResponse> createInitialDepreciation(Asset asset);

    Mono<DepreciationResponse> getById(UUID id);

    Flux<DepreciationResponse> getAll();

    Flux<DepreciationResponse> getByAssetId(UUID assetId);

    Flux<DepreciationResponse> getByFiscalYear(Integer fiscalYear);

    Mono<DepreciationResponse> getByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth);

    Mono<Void> delete(UUID id);
    
    Mono<Void> deleteByAssetAndPeriod(UUID assetId, Integer fiscalYear, Integer calculationMonth);

    Mono<DepreciationResponse> approve(UUID id, UUID approvedBy);

    Flux<DepreciationResponse> generateAutomaticDepreciations(
            UUID assetId,
            BigDecimal initialValue,
            BigDecimal residualValue,
            int usefulLifeMonths,
            LocalDateTime acquisitionDate);
}
