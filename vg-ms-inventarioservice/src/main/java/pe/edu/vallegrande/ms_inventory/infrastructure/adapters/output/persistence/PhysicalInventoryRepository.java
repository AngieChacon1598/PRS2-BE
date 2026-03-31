package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;

import java.util.UUID;

@Repository
public interface PhysicalInventoryRepository extends ReactiveCrudRepository<PhysicalInventory, UUID> {
}
