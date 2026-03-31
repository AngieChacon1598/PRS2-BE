package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalDetailUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-disposal-details")
@RequiredArgsConstructor
public class AssetDisposalDetailRestController {

    private final AssetDisposalDetailUseCase assetDisposalDetailUseCase;

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public Mono<AssetDisposalDetailResponse> create(@RequestBody AssetDisposalDetailRequest request) {
        return assetDisposalDetailUseCase.create(request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{id}")
    public Mono<AssetDisposalDetailResponse> getById(@PathVariable UUID id) {
        return assetDisposalDetailUseCase.getById(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/disposal/{disposalId}")
    public Flux<AssetDisposalDetailResponse> getByDisposalId(@PathVariable UUID disposalId) {
        return assetDisposalDetailUseCase.getByDisposalId(disposalId);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/asset/{assetId}")
    public Flux<AssetDisposalDetailResponse> getByAssetId(@PathVariable UUID assetId) {
        return assetDisposalDetailUseCase.getByAssetId(assetId);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}/technical-opinion")
    public Mono<AssetDisposalDetailResponse> addTechnicalOpinion(
            @PathVariable UUID id,
            @RequestBody TechnicalOpinionRequest request) {
        return assetDisposalDetailUseCase.addTechnicalOpinion(id, request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}/execute-removal")
    public Mono<AssetDisposalDetailResponse> executeRemoval(
            @PathVariable UUID id,
            @RequestBody ExecuteRemovalRequest request) {
        return assetDisposalDetailUseCase.executeRemoval(id, request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable UUID id) {
        return assetDisposalDetailUseCase.delete(id);
    }

    @GetMapping("/active-asset-ids")
    public Flux<UUID> getActiveAssetIds() {
        return assetDisposalDetailUseCase.findActiveAssetIds();
    }
}
