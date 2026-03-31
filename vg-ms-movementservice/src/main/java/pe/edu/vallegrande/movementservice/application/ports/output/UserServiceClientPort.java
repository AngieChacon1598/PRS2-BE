package pe.edu.vallegrande.movementservice.application.ports.output;

import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserServiceClientPort {
    Mono<UserResponse> getUserById(UUID userId);
}
