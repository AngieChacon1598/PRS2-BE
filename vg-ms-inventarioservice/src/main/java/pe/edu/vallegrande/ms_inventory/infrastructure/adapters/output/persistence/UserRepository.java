package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.ms_inventory.domain.model.User;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
}
