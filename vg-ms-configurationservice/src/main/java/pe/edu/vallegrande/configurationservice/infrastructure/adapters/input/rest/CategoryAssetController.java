package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.domain.model.CategoryAsset;
import pe.edu.vallegrande.configurationservice.application.service.CategoryAssetService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/categories-assets")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:categories:manage') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class CategoryAssetController {

    private final CategoryAssetService service;


    @GetMapping
    public Flux<CategoryAsset> getAll() {
        return service.getAll();
    }


    @GetMapping("/active")
    public Flux<CategoryAsset> getAllActive() {
        return service.getAllActive();
    }


    @GetMapping("/inactive")
    public Flux<CategoryAsset> getAllInactive() {
        return service.getAllInactive();
    }


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CategoryAsset> create(@RequestBody CategoryAsset category) {
        return service.create(category);
    }


    @PutMapping("/update/{id}")
    public Mono<CategoryAsset> update(@PathVariable UUID id, @RequestBody CategoryAsset category) {
        return service.update(id, category);
    }


    @DeleteMapping("/inactive/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> softDelete(@PathVariable UUID id) {
        return service.softDelete(id).then();
    }


    @PatchMapping("/restore/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> restore(@PathVariable UUID id) {
        return service.restore(id).then();
    }
}