package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.Area;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AreaRepository extends ReactiveCrudRepository<Area, UUID> {
    Flux<Area> findAllByActiveTrueOrderByNameAsc();
    Flux<Area> findAllByActiveFalseOrderByNameAsc();
    Mono<Void> deleteByAreaCode(String areaCode);
}
