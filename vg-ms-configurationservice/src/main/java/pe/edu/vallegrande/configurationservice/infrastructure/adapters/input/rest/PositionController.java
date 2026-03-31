package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.domain.model.Position;
import pe.edu.vallegrande.configurationservice.application.service.PositionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.UUID;


import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:manage:positions') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class PositionController {


    private final PositionService service;


    @GetMapping
    public Flux<Position> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/inactive")
    public Flux<Position> getAllInactive() {
        return service.getAllInactive();
    }

    @GetMapping("/{id}")
    public Mono<Position> getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Position> create(@RequestBody Position position) {
        return service.create(position);
    }

    @PutMapping("/{id}")
    public Mono<Position> update(@PathVariable UUID id, @RequestBody Position position) {
        return service.update(id, position);
    }

    @DeleteMapping("/{id}")
    public Mono<Position> softDelete(@PathVariable UUID id) {
        return service.softDelete(id);
    }

    @PatchMapping("/{id}/restore")
    public Mono<Position> restore(@PathVariable UUID id) {
        return service.restore(id);
    }
}
