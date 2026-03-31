package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pe.edu.vallegrande.ms_inventory.application.ports.out.PhysicalInventoryPersistencePort;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PhysicalInventoryPersistenceAdapter implements PhysicalInventoryPersistencePort {

    private final PhysicalInventoryRepository repository;

    @Override
    public Mono<PhysicalInventory> save(PhysicalInventory inventory) {
        return repository.save(inventory);
    }

    @Override
    public Mono<PhysicalInventory> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Flux<PhysicalInventory> findAll() {
        return repository.findAll();
    }
}
