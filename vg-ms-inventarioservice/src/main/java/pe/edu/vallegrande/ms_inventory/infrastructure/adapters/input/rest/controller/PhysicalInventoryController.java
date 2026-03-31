package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.input.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_inventory.application.dto.*;
import pe.edu.vallegrande.ms_inventory.application.ports.in.PhysicalInventoryUseCase;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class PhysicalInventoryController {
    private final PhysicalInventoryUseCase useCase;

    @GetMapping
    @PreAuthorize("hasAuthority('inventario:read')")
    public Flux<PhysicalInventory> getAll() {
        return useCase.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventario:read')")
    public Mono<PhysicalInventory> getById(@PathVariable UUID id) {
        return useCase.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventario:create')")
    public Mono<PhysicalInventoryDTO> create(@RequestBody PhysicalInventoryDTO dto) {
        return useCase.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventario:update')")
    public Mono<PhysicalInventory> update(@PathVariable UUID id, @RequestBody PhysicalInventory inventory) {
        return useCase.update(id, inventory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventario:update')")
    public Mono<ResponseEntity<String>> deleteLogical(@PathVariable UUID id, @RequestParam UUID userId) {
        return useCase.deleteLogical(id)
                .then(Mono.just(ResponseEntity.ok("Inventario eliminado correctamente")))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error al eliminar inventario: " + ex.getMessage())));
    }

    @GetMapping("/with-details")
    @PreAuthorize("hasAuthority('inventario:read')")
    public Flux<PhysicalInventory> getAllWithDetails() {
        return useCase.listAllWithDetails();
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAuthority('inventario:update')")
    public Mono<PhysicalInventory> startInventory(@PathVariable UUID id, @RequestParam UUID userId) {
        return useCase.startInventory(id);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('inventario:close')")
    public Mono<PhysicalInventory> completeInventory(@PathVariable UUID id, @RequestParam UUID userId) {
        return useCase.completeInventory(id);
    }

    @GetMapping("/form-data")
    @PreAuthorize("hasAuthority('inventario:read')")
    public Mono<InventoryFormDataDTO> getFormData() {
        return useCase.getFormData();
    }
}