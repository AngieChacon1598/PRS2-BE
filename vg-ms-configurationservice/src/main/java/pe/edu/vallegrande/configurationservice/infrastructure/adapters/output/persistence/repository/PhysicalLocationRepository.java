package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.PhysicalLocation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface PhysicalLocationRepository extends ReactiveCrudRepository<PhysicalLocation, UUID> {
    Flux<PhysicalLocation> findAllByActiveTrueOrderByNameAsc();
    Flux<PhysicalLocation> findAllByActiveFalseOrderByNameAsc();
    Mono<Void> deleteByLocationCode(String locationCode);
}
