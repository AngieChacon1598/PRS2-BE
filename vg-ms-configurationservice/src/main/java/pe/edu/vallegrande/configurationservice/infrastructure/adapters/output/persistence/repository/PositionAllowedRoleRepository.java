package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.configurationservice.domain.model.PositionAllowedRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PositionAllowedRoleRepository extends ReactiveCrudRepository<PositionAllowedRole, UUID> {

    // 🔹 Buscar por cargo
    Flux<PositionAllowedRole> findAllByPositionId(UUID positionId);

    // 🔹 Buscar por municipio
    Flux<PositionAllowedRole> findAllByMunicipalityId(UUID municipalityId);

    // 🔹 Buscar por cargo y municipio
    Flux<PositionAllowedRole> findAllByPositionIdAndMunicipalityId(UUID positionId, UUID municipalityId);

    // 🔹 Buscar roles por defecto de un municipio
    Flux<PositionAllowedRole> findAllByMunicipalityIdAndIsDefaultTrue(UUID municipalityId);

    // 🔹 Verificar duplicado antes de insertar
    Mono<PositionAllowedRole> findByPositionIdAndAreaIdAndRoleIdAndMunicipalityId(
            UUID positionId, UUID areaId, UUID roleId, UUID municipalityId);
}
