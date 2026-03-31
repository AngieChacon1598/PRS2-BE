package pe.edu.vallegrande.patrimonio_service.application.ports.input;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AssetServicePort {
    
    Mono<AssetResponse> create(AssetRequest request);
    
    Mono<AssetResponse> getById(UUID id);
    
    Flux<AssetResponse> getAll();
    
    Mono<AssetResponse> update(UUID id, AssetRequest request);
    
    Mono<Void> delete(UUID id);
    
    Mono<AssetResponse> changeStatus(UUID id, CambioEstadoRequest request);
    
    Flux<AssetResponse> findByStatus(String status);
    
    Mono<AssetResponse> findByAssetCode(String assetCode);
}
