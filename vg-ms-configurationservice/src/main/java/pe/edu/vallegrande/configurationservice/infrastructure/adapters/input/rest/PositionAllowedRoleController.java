package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.application.dto.PositionAllowedRoleRequest;
import pe.edu.vallegrande.configurationservice.application.service.PositionAllowedRoleService;
import pe.edu.vallegrande.configurationservice.domain.model.PositionAllowedRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/position-allowed-roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:manage:positions') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class PositionAllowedRoleController {

    private final PositionAllowedRoleService service;

    @GetMapping
    public Flux<PositionAllowedRole> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Mono<PositionAllowedRole> getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/position/{positionId}")
    public Flux<PositionAllowedRole> getByPositionId(@PathVariable UUID positionId) {
        return service.getByPositionId(positionId);
    }

    @GetMapping("/municipality/{municipalityId}")
    public Flux<PositionAllowedRole> getByMunicipalityId(@PathVariable UUID municipalityId) {
        return service.getByMunicipalityId(municipalityId);
    }

    @GetMapping("/position/{positionId}/municipality/{municipalityId}")
    public Flux<PositionAllowedRole> getByPositionAndMunicipality(
            @PathVariable UUID positionId,
            @PathVariable UUID municipalityId) {
        return service.getByPositionAndMunicipality(positionId, municipalityId);
    }

    @GetMapping("/municipality/{municipalityId}/defaults")
    public Flux<PositionAllowedRole> getDefaultsByMunicipality(@PathVariable UUID municipalityId) {
        return service.getDefaultsByMunicipality(municipalityId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PositionAllowedRole> create(@Valid @RequestBody PositionAllowedRoleRequest request) {
        return service.create(toEntity(request));
    }

    @PutMapping("/{id}")
    public Mono<PositionAllowedRole> update(@PathVariable UUID id, @Valid @RequestBody PositionAllowedRoleRequest request) {
        return service.update(id, toEntity(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return service.delete(id);
    }

    private PositionAllowedRole toEntity(PositionAllowedRoleRequest req) {
        return PositionAllowedRole.builder()
                .positionId(req.getPositionId())
                .areaId(req.getAreaId())
                .roleId(req.getRoleId())
                .isDefault(req.getIsDefault() != null ? req.getIsDefault() : false)
                .municipalityId(req.getMunicipalityId())
                .build();
    }
}
