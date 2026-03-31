package pe.edu.vallegrande.ms_inventory.application.ports.in;

import java.util.UUID;

import pe.edu.vallegrande.ms_inventory.application.dto.InventoryFormDataDTO;
import pe.edu.vallegrande.ms_inventory.application.dto.PhysicalInventoryDTO;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhysicalInventoryUseCase {

    Flux<PhysicalInventory> listAll();

    Mono<PhysicalInventory> getById(UUID id);

    Mono<PhysicalInventoryDTO> create(PhysicalInventoryDTO dto);

    Mono<PhysicalInventory> update(UUID id, PhysicalInventory inventory);

    Mono<Void> deleteLogical(UUID id);

    Flux<PhysicalInventory> listAllWithDetails();

    Mono<PhysicalInventory> startInventory(UUID id);

    Mono<PhysicalInventory> completeInventory(UUID id);

    Mono<InventoryFormDataDTO> getFormData();
}
