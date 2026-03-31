package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.configurationservice.domain.model.Position;
import reactor.core.publisher.Flux;


import java.util.UUID;


public interface PositionRepository extends ReactiveCrudRepository<Position, UUID> {


    // 🔹 Fetch only active positions
    Flux<Position> findAllByActiveTrue();


    // 🔹 Fetch only inactive positions
    Flux<Position> findAllByActiveFalse();


    // 🔹 Optional: find by position code
    Flux<Position> findAllByPositionCode(String positionCode);
}


