package pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.configurationservice.application.dto.SupplierRequest;
import pe.edu.vallegrande.configurationservice.application.service.SupplierService;
import pe.edu.vallegrande.configurationservice.domain.model.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('config:manage:suppliers') or hasAuthority('config:read') or hasRole('TENANT_ADMIN')")
public class SupplierController {

    private final SupplierService service;

    @GetMapping
    public Flux<Supplier> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/inactive")
    public Flux<Supplier> getAllInactive() {
        return service.getAllInactive();
    }

    @GetMapping("/{id}")
    public Mono<Supplier> getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Supplier> create(@Valid @RequestBody SupplierRequest request) {
        return service.create(toEntity(request));
    }

    @PutMapping("/{id}")
    public Mono<Supplier> update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
        return service.update(id, toEntity(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> softDelete(@PathVariable UUID id) {
        return service.softDelete(id);
    }

    @PatchMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> restore(@PathVariable UUID id) {
        return service.restore(id);
    }

    private Supplier toEntity(SupplierRequest req) {
        return Supplier.builder()
                .documentTypesId(req.getDocumentTypesId())
                .numeroDocumento(req.getNumeroDocumento())
                .legalName(req.getLegalName())
                .tradeName(req.getTradeName())
                .address(req.getAddress())
                .phone(req.getPhone())
                .email(req.getEmail())
                .website(req.getWebsite())
                .mainContact(req.getMainContact())
                .companyType(req.getCompanyType())
                .isStateProvider(req.getIsStateProvider())
                .classification(req.getClassification())
                .municipalityId(req.getMunicipalityId())
                .build();
    }
}
