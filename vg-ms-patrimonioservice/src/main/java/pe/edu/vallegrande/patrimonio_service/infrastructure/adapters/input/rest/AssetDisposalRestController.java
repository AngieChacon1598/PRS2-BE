package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-disposals")
@RequiredArgsConstructor
public class AssetDisposalRestController {

    private final AssetDisposalUseCase assetDisposalUseCase;

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public Mono<AssetDisposalResponse> create(@RequestBody AssetDisposalRequest request) {
        return assetDisposalUseCase.create(request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public Mono<AssetDisposalResponse> getById(@PathVariable UUID id) {
        return assetDisposalUseCase.getById(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public Flux<AssetDisposalResponse> getAll() {
        return assetDisposalUseCase.getAll();
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'AUDITORIA_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/status/{status}")
    public Flux<AssetDisposalResponse> getByStatus(@PathVariable String status) {
        return assetDisposalUseCase.getByStatus(status);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'AUDITORIA_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/file-number/{fileNumber}")
    public Mono<AssetDisposalResponse> getByFileNumber(@PathVariable String fileNumber) {
        return assetDisposalUseCase.getByFileNumber(fileNumber);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/requested-by/{userId}")
    public Flux<AssetDisposalResponse> getByRequestedBy(@PathVariable UUID userId) {
        return assetDisposalUseCase.getByRequestedBy(userId);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}/assign-committee")
    public Mono<AssetDisposalResponse> assignCommittee(
            @PathVariable UUID id,
            @RequestBody AssignCommitteeRequest request) {
        return assetDisposalUseCase.assignCommittee(id, request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}/resolve")
    public Mono<AssetDisposalResponse> resolve(
            @PathVariable UUID id,
            @RequestBody ResolveDisposalRequest request) {
        return assetDisposalUseCase.resolve(id, request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/cancel")
    public Mono<AssetDisposalResponse> cancel(
            @PathVariable UUID id,
            @RequestParam UUID cancelledBy) {
        return assetDisposalUseCase.cancel(id, cancelledBy);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/finalize")
    public Mono<AssetDisposalResponse> finalize(@PathVariable UUID id) {
        return assetDisposalUseCase.finalize(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/restore")
    public Mono<AssetDisposalResponse> restore(@PathVariable UUID id) {
        return assetDisposalUseCase.restore(id);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetDisposalUseCase.delete(id);
    }
}
