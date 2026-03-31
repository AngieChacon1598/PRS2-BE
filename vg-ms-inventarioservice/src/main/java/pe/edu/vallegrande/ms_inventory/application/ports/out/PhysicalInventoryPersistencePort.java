package pe.edu.vallegrande.ms_inventory.application.ports.out;

import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PhysicalInventoryPersistencePort {

    Mono<PhysicalInventory> save(PhysicalInventory inventory);

    Mono<PhysicalInventory> findById(UUID id);

    Flux<PhysicalInventory> findAll();
}
