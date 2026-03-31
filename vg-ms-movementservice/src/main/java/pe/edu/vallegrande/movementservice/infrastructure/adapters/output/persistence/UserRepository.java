package pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.movementservice.domain.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    @Query("SELECT * FROM users WHERE municipality_id = :municipalityId AND status = :status ORDER BY username")
    Flux<User> findByMunicipalityIdAndStatus(UUID municipalityId, String status);

    @Query("SELECT * FROM users WHERE id = :id AND municipality_id = :municipalityId")
    Mono<User> findByIdAndMunicipalityId(UUID id, UUID municipalityId);

    @Query("SELECT * FROM users WHERE username = :username AND municipality_id = :municipalityId")
    Mono<User> findByUsernameAndMunicipalityId(String username, UUID municipalityId);
}
