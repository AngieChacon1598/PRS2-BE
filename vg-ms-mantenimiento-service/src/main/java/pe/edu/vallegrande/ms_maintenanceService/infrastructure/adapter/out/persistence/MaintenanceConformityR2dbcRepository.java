package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MaintenanceConformityR2dbcRepository extends ReactiveCrudRepository<MaintenanceConformityEntity, UUID> {
    Mono<MaintenanceConformityEntity> findByMaintenanceId(UUID maintenanceId);
    Mono<MaintenanceConformityEntity> findByConformityNumber(String conformityNumber);

    @org.springframework.data.r2dbc.repository.Query("SELECT COUNT(*) FROM maintenance_conformity WHERE municipality_id = :municipalityId AND EXTRACT(YEAR FROM created_at) = :year")
    Mono<Long> countByMunicipalityIdAndYear(UUID municipalityId, int year);
}
