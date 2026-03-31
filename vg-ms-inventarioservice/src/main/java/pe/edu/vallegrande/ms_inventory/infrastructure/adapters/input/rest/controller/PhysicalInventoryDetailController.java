package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pe.edu.vallegrande.ms_inventory.application.ports.in.PhysicalInventoryDetailUseCase;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory-details")
@RequiredArgsConstructor
public class PhysicalInventoryDetailController {

    private final PhysicalInventoryDetailUseCase useCase;

    @GetMapping
    @PreAuthorize("hasAuthority('inventario:read')")
    public Flux<PhysicalInventoryDetail> getAll() {
        return useCase.listAll();
    }

    @GetMapping("/by-inventory/{inventoryId}")
    @PreAuthorize("hasAuthority('inventario:read')")
    public Flux<PhysicalInventoryDetail> getByInventoryId(@PathVariable UUID inventoryId) {
        return useCase.listByInventoryId(inventoryId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventario:verify')")
    public Mono<PhysicalInventoryDetail> create(@RequestBody PhysicalInventoryDetail detail) {
        return useCase.create(detail);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventario:verify')")
    public Mono<PhysicalInventoryDetail> update(@PathVariable UUID id, @RequestBody PhysicalInventoryDetail detail) {
        return useCase.update(id, detail);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventario:update')")
    public Mono<Void> deleteLogical(@PathVariable UUID id) {
        return useCase.deleteLogical(id);
    }
}
