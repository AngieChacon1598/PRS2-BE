package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PhysicalInventoryDetailRepository extends ReactiveCrudRepository<PhysicalInventoryDetail, UUID> {

    Flux<PhysicalInventoryDetail> findByInventoryId(UUID inventoryId);
}
