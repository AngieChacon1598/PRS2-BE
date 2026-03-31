package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AssetRepository extends ReactiveCrudRepository<Asset, UUID> {
    
    Mono<Asset> findByAssetCode(String assetCode);
    
    Flux<Asset> findByAssetStatus(String assetStatus);
    
    Flux<Asset> findByCategoryId(UUID categoryId);
    
    Flux<Asset> findByCurrentLocationId(UUID locationId);
    
    Flux<Asset> findByCurrentResponsibleId(UUID responsibleId);
    
    Mono<Long> countByAssetStatus(String assetStatus);
    
    Mono<Boolean> existsByAssetCode(String assetCode);

    @Query("UPDATE assets SET asset_status = :status, updated_at = :updatedAt WHERE id = :id")
    Mono<Integer> updateAssetStatus(UUID id, String status, LocalDateTime updatedAt);
}
