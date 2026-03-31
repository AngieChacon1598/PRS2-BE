package pe.edu.vallegrande.ms_inventory.application.ports.in;

import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PhysicalInventoryDetailUseCase {

    Flux<PhysicalInventoryDetail> listAll();

    Flux<PhysicalInventoryDetail> listByInventoryId(UUID inventoryId);

    Mono<PhysicalInventoryDetail> create(PhysicalInventoryDetail detail);

    Mono<PhysicalInventoryDetail> update(UUID id, PhysicalInventoryDetail detail);

    Mono<Void> deleteLogical(UUID id);
}
