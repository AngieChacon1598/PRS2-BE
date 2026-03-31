package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.domain.model.PhysicalLocation;
import pe.edu.vallegrande.configurationservice.application.service.PhysicalLocationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/physical-locations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:locations:manage') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class PhysicalLocationsController {

    private final PhysicalLocationService service;

    @GetMapping
    public Flux<PhysicalLocation> getAllRoot() {
        return service.getAll();
    }

    @GetMapping("/GetAll")
    public Flux<PhysicalLocation> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public Flux<PhysicalLocation> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/inactive")
    public Flux<PhysicalLocation> getAllInactive() {
        return service.getAllInactive();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PhysicalLocation> create(@RequestBody PhysicalLocation entity) {
        return service.create(entity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PhysicalLocation> createAtRoot(@RequestBody PhysicalLocation entity) {
        return service.create(entity);
    }

    @PutMapping("/update/{id}")
    public Mono<PhysicalLocation> update(@PathVariable UUID id, @RequestBody PhysicalLocation entity) {
        return service.update(id, entity);
    }

    @DeleteMapping("/inactive/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> softDelete(@PathVariable UUID id) {
        return service.softDelete(id);
    }

    @PatchMapping("/restore/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> restore(@PathVariable UUID id) {
        return service.restore(id);
    }

    // Hard delete (permanent)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> hardDeleteById(@PathVariable UUID id) {
        return service.hardDeleteById(id);
    }

    @DeleteMapping("/by-code/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> hardDeleteByCode(@PathVariable String code) {
        return service.hardDeleteByCode(code);
    }
}
