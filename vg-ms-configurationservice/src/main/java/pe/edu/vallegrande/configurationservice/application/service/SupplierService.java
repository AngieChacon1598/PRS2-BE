package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.exception.DuplicateDocumentException;
import pe.edu.vallegrande.configurationservice.domain.model.Supplier;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.SupplierRepository;
import pe.edu.vallegrande.configurationservice.infrastructure.config.JwtContextHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;
    private final JwtContextHelper jwtContextHelper;

    public Flux<Supplier> getAllActive() {
        return repository.findAllByActiveTrueOrderByLegalNameAsc();
    }

    public Flux<Supplier> getAllInactive() {
        return repository.findAllByActiveFalseOrderByLegalNameAsc();
    }

    public Mono<Supplier> getById(UUID id) {
        return repository.findById(id);
    }

    public Mono<Supplier> create(Supplier supplier) {
        return jwtContextHelper.getMunicipalityId()
                .flatMap(municipalityId -> {
                    supplier.setMunicipalityId(municipalityId);
                    return repository.findByDocumentTypesIdAndNumeroDocumento(
                                    supplier.getDocumentTypesId(), supplier.getNumeroDocumento())
                            .flatMap(existing -> Mono.<Supplier>error(
                                    new DuplicateDocumentException(
                                            supplier.getDocumentTypesId(), supplier.getNumeroDocumento())))
                            .switchIfEmpty(Mono.defer(() -> {
                                supplier.setActive(true);
                                supplier.setCreatedAt(OffsetDateTime.now());
                                supplier.setUpdatedAt(OffsetDateTime.now());
                                return repository.save(supplier);
                            }));
                });
    }

    public Mono<Supplier> update(UUID id, Supplier supplierData) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (!existing.getDocumentTypesId().equals(supplierData.getDocumentTypesId()) ||
                            !existing.getNumeroDocumento().equals(supplierData.getNumeroDocumento())) {
                        return repository.findByDocumentTypesIdAndNumeroDocumento(
                                        supplierData.getDocumentTypesId(), supplierData.getNumeroDocumento())
                                .flatMap(duplicate -> Mono.<Supplier>error(
                                        new DuplicateDocumentException(
                                                supplierData.getDocumentTypesId(), supplierData.getNumeroDocumento())))
                                .switchIfEmpty(Mono.defer(() -> updateSupplierData(existing, supplierData)));
                    }
                    return updateSupplierData(existing, supplierData);
                });
    }

    private Mono<Supplier> updateSupplierData(Supplier existing, Supplier data) {
        existing.setDocumentTypesId(data.getDocumentTypesId());
        existing.setNumeroDocumento(data.getNumeroDocumento());
        existing.setLegalName(data.getLegalName());
        existing.setTradeName(data.getTradeName());
        existing.setAddress(data.getAddress());
        existing.setPhone(data.getPhone());
        existing.setEmail(data.getEmail());
        existing.setWebsite(data.getWebsite());
        existing.setMainContact(data.getMainContact());
        existing.setCompanyType(data.getCompanyType());
        existing.setIsStateProvider(data.getIsStateProvider());
        existing.setClassification(data.getClassification());
        existing.setUpdatedAt(OffsetDateTime.now());
        return repository.save(existing);
    }

    public Mono<Void> softDelete(UUID id) {
        return repository.findById(id)
                .flatMap(supplier -> {
                    supplier.setActive(false);
                    supplier.setUpdatedAt(OffsetDateTime.now());
                    return repository.save(supplier);
                })
                .then();
    }

    public Mono<Void> restore(UUID id) {
        return repository.findById(id)
                .flatMap(supplier -> {
                    supplier.setActive(true);
                    supplier.setUpdatedAt(OffsetDateTime.now());
                    return repository.save(supplier);
                })
                .then();
    }
}
