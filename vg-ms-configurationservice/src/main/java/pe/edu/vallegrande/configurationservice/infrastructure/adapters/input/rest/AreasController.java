package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.application.dto.AreaRequest;
import pe.edu.vallegrande.configurationservice.application.service.AreaService;
import pe.edu.vallegrande.configurationservice.domain.model.Area;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/areas")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:areas:manage') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class AreasController {

    private final AreaService service;

    @GetMapping
    public Flux<Area> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public Flux<Area> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/inactive")
    public Flux<Area> getAllInactive() {
        return service.getAllInactive();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Area> create(@Valid @RequestBody AreaRequest request) {
        return service.create(toEntity(request));
    }

    @PutMapping("/{id}")
    public Mono<Area> update(@PathVariable UUID id, @Valid @RequestBody AreaRequest request) {
        return service.update(id, toEntity(request));
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

    private Area toEntity(AreaRequest req) {
        return Area.builder()
                .municipalityId(req.getMunicipalityId())
                .areaCode(req.getAreaCode())
                .name(req.getName())
                .description(req.getDescription())
                .parentAreaId(req.getParentAreaId())
                .hierarchicalLevel(req.getHierarchicalLevel())
                .responsibleId(req.getResponsibleId())
                .physicalLocation(req.getPhysicalLocation())
                .phone(req.getPhone())
                .email(req.getEmail())
                .annualBudget(req.getAnnualBudget())
                .build();
    }
}
