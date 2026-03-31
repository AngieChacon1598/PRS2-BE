package pe.edu.vallegrande.movementservice.application.ports.output;

import pe.edu.vallegrande.movementservice.application.dto.AssetUpdateRequest;
import pe.edu.vallegrande.movementservice.application.dto.MovementNotificationRequest;
import reactor.core.publisher.Mono;

public interface AssetServiceClientPort {
    Mono<Void> notifyNewMovement(MovementNotificationRequest request);
    Mono<Void> updateAssetLocation(AssetUpdateRequest request);
}
