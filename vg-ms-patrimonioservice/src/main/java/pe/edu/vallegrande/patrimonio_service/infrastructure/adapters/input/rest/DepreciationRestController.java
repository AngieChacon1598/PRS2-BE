package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.input.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.DepreciationUseCase;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.DepreciationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets-depreciations")
public class DepreciationRestController {

    private final DepreciationUseCase depreciationUseCase;

    public DepreciationRestController(DepreciationUseCase depreciationUseCase) {
        this.depreciationUseCase = depreciationUseCase;
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DepreciationResponse> create(@RequestBody DepreciationRequest request) {
        return depreciationUseCase.create(request);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{assetId}")
    public Flux<DepreciationResponse> getByAssetIdNormal(@PathVariable UUID assetId) {
        return depreciationUseCase.getByAssetId(assetId)
                .switchIfEmpty(Flux.empty());
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public Flux<DepreciationResponse> getAll() {
        return depreciationUseCase.getAll();
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/asset/{assetId}")
    public Flux<DepreciationResponse> getByAssetId(@PathVariable UUID assetId) {
        return depreciationUseCase.getByAssetId(assetId);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/year/{fiscalYear}")
    public Flux<DepreciationResponse> getByFiscalYear(@PathVariable Integer fiscalYear) {
        return depreciationUseCase.getByFiscalYear(fiscalYear);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'PATRIMONIO_OPERARIO', 'PATRIMONIO_VIEWER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/asset/{assetId}/year/{fiscalYear}/month/{calculationMonth}")
    public Mono<DepreciationResponse> getByAssetAndPeriod(
            @PathVariable UUID assetId,
            @PathVariable Integer fiscalYear,
            @PathVariable Integer calculationMonth) {
        return depreciationUseCase.getByAssetAndPeriod(assetId, fiscalYear, calculationMonth);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return depreciationUseCase.delete(id);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @PatchMapping("/{id}/approve")
    public Mono<DepreciationResponse> approve(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy) {
        return depreciationUseCase.approve(id, approvedBy);
    }

    @PreAuthorize("hasAnyRole('PATRIMONIO_GESTOR', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/auto/{assetId}")
    public Mono<org.springframework.http.ResponseEntity<?>> generateAutomaticDepreciations(
            @PathVariable UUID assetId,
            @RequestParam BigDecimal initialValue,
            @RequestParam BigDecimal residualValue,
            @RequestParam int usefulLifeMonths,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime acquisitionDate) {

        return depreciationUseCase
                .generateAutomaticDepreciations(assetId, initialValue, residualValue, usefulLifeMonths, acquisitionDate)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.just(org.springframework.http.ResponseEntity
                                .status(HttpStatus.OK)
                                .body(" Aún no se han generado depreciaciones, ya que no ha pasado un mes desde la adquisición."));
                    } else {
                        return Mono.just(org.springframework.http.ResponseEntity
                                .status(HttpStatus.OK)
                                .body(list));
                    }
                })
                .onErrorResume(e -> Mono.just(org.springframework.http.ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(" Error al generar las depreciaciones: " + e.getMessage())));
    }
}
