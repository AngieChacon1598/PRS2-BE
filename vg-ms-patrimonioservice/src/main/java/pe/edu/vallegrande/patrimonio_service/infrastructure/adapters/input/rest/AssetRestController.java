package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetRestController {

    private final AssetUseCase assetUseCase;

    public AssetRestController(AssetUseCase assetUseCase) {
        this.assetUseCase = assetUseCase;
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AssetResponse> create(@RequestBody AssetRequest request) {
        return assetUseCase.create(request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public Mono<AssetResponse> getById(@PathVariable UUID id) {
        return assetUseCase.getById(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public Flux<AssetResponse> getAll() {
        return assetUseCase.getAll();
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public Mono<AssetResponse> update(@PathVariable UUID id, @RequestBody AssetRequest request) {
        return assetUseCase.update(id, request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetUseCase.delete(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/status")
    public Mono<AssetResponse> changeStatus(@PathVariable UUID id, @RequestBody CambioEstadoRequest request) {
        return assetUseCase.changeStatus(id, request);
    }

    @GetMapping("/status/{status}")
    public Flux<AssetResponse> findByStatus(@PathVariable String status) {
        return assetUseCase.findByStatus(status);
    }

    @GetMapping("/code/{assetCode}")
    public Mono<AssetResponse> findByAssetCode(@PathVariable String assetCode) {
        return assetUseCase.findByAssetCode(assetCode);
    }
}
