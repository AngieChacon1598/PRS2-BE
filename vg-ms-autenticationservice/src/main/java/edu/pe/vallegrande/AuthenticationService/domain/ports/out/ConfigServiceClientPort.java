package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import reactor.core.publisher.Flux;
import java.util.UUID;

public interface ConfigServiceClientPort {
    /**
     * Obtiene los IDs de los roles por defecto según el cargo y área del usuario.
     */
    Flux<UUID> getDefaultRolesByContext(UUID positionId, UUID areaId, UUID municipalityId);
}
