package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MaintenancePartR2dbcRepository extends ReactiveCrudRepository<MaintenancePartEntity, UUID> {
    Flux<MaintenancePartEntity> findByMaintenanceId(UUID maintenanceId);
    Flux<MaintenancePartEntity> findByMunicipalityId(UUID municipalityId);
}
