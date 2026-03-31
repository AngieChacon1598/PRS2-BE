package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AssetService implements AssetUseCase {

    private final AssetPersistencePort persistencePort;

    public AssetService(AssetPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Mono<AssetResponse> create(AssetRequest request) {
        Asset asset = new Asset();
        BeanUtils.copyProperties(request, asset);

        // Set default values
        asset.setCreatedAt(LocalDateTime.now());

        // If created_by is not provided, use a default UUID
        if (asset.getCreatedBy() == null) {
            asset.setCreatedBy(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }

        // Initial monetary values
        if (asset.getCurrentValue() == null) {
            asset.setCurrentValue(asset.getAcquisitionValue());
        }

        return persistencePort.save(asset)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetResponse> getById(UUID id) {
        return persistencePort.findById(id)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)));
    }

    @Override
    public Flux<AssetResponse> getAll() {
        return persistencePort.findAll()
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetResponse> update(UUID id, AssetRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(existing -> {
                    BeanUtils.copyProperties(request, existing, "id", "createdBy", "createdAt");
                    existing.setUpdatedAt(LocalDateTime.now());
                    
                    return persistencePort.save(existing);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(asset -> persistencePort.deleteById(id));
    }

    @Override
    public Mono<AssetResponse> changeStatus(UUID id, CambioEstadoRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with ID: " + id)))
                .flatMap(asset -> {
                    asset.setAssetStatus(request.getNuevoEstado());
                    asset.setObservations(request.getObservaciones());
                    asset.setUpdatedAt(LocalDateTime.now());
                    
                    if (request.getModificadoPor() != null) {
                        asset.setUpdatedBy(request.getModificadoPor());
                    }
                    
                    return persistencePort.save(asset);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Flux<AssetResponse> findByStatus(String status) {
        return persistencePort.findByAssetStatus(status)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetResponse> findByAssetCode(String assetCode) {
        return persistencePort.findByAssetCode(assetCode)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetNotFoundException("Asset not found with code: " + assetCode)));
    }

    private AssetResponse convertToResponse(Asset asset) {
        AssetResponse response = new AssetResponse();
        BeanUtils.copyProperties(asset, response);
        return response;
    }
}
