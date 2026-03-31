package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MaintenanceStatusHistoryR2dbcRepository extends ReactiveCrudRepository<MaintenanceStatusHistoryEntity, UUID> {
    Flux<MaintenanceStatusHistoryEntity> findByMaintenanceIdOrderByChangedAtDesc(UUID maintenanceId);
}
