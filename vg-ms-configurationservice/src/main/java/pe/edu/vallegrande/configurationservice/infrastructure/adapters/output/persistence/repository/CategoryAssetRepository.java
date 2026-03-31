package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.configurationservice.domain.model.CategoryAsset;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CategoryAssetRepository extends ReactiveCrudRepository<CategoryAsset, UUID> {
    Flux<CategoryAsset> findByActiveTrueOrderByNameAsc();
    Flux<CategoryAsset> findByActiveFalseOrderByNameAsc();
}