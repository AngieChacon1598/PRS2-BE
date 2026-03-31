package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.application.dto.ConfiguracionTenantDTO;
import pe.edu.vallegrande.configurationservice.application.service.ConfiguracionTenantService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/configuration-tenant")
@RequiredArgsConstructor
@Tag(name = "Configuración Tenant", description = "API para gestionar la configuración de tenants")
@PreAuthorize("hasAuthority('config:manage:tenant') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class ConfiguracionTenantController {

    private final ConfiguracionTenantService service;

    @GetMapping
    @Operation(summary = "Listar todas las configuraciones de tenant")
    public Flux<ConfiguracionTenantDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener configuración de tenant por ID")
    public Mono<ConfiguracionTenantDTO> findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @GetMapping("/municipality/{municipalityId}")
    @Operation(summary = "Obtener configuración por ID de municipalidad")
    public Mono<ConfiguracionTenantDTO> findByMunicipalityId(@PathVariable UUID municipalityId) {
        return service.findByMunicipalityId(municipalityId);
    }

    @GetMapping("/active/{active}")
    @Operation(summary = "Listar configuraciones por estado activo")
    public Flux<ConfiguracionTenantDTO> findByActive(@PathVariable Boolean active) {
        return service.findByActive(active);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva configuración de tenant")
    public Mono<ConfiguracionTenantDTO> create(@RequestBody ConfiguracionTenantDTO dto) {
        return service.save(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar configuración de tenant")
    public Mono<ConfiguracionTenantDTO> update(@PathVariable UUID id, @RequestBody ConfiguracionTenantDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar configuración de tenant")
    public Mono<Void> delete(@PathVariable UUID id) {
        return service.deleteById(id);
    }
}
