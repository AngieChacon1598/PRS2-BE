package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.domain.model.SystemConfiguration;
import pe.edu.vallegrande.configurationservice.application.service.SystemConfigurationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/system-configurations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:manage:system') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class SystemConfigurationController {

    private final SystemConfigurationService service;


    @GetMapping
    public Flux<SystemConfiguration> getAll() {
        return service.getAll();
    }


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SystemConfiguration> create(@RequestBody SystemConfiguration config) {
        return service.create(config);
    }


    @PutMapping("/update/{id}")
    public Mono<SystemConfiguration> update(@PathVariable UUID id, @RequestBody SystemConfiguration config) {
        return service.update(id, config);
    }


    @DeleteMapping("/soft-delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> softDelete(@PathVariable UUID id) {
        return service.softDelete(id);
    }


    @PutMapping("/restore/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> restore(@PathVariable UUID id) {
        return service.restore(id);
    }
}