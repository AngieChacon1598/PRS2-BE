package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaintenanceR2dbcRepository extends ReactiveCrudRepository<MaintenanceEntity, UUID> {

    Mono<MaintenanceEntity> findByMaintenanceCode(String maintenanceCode);

    Flux<MaintenanceEntity> findByMunicipalityId(UUID municipalityId);

    Flux<MaintenanceEntity> findByAssetId(UUID assetId);

    Flux<MaintenanceEntity> findByMaintenanceType(String maintenanceType);

    Flux<MaintenanceEntity> findByMaintenanceStatus(String maintenanceStatus);

    Flux<MaintenanceEntity> findByMaintenanceStatusAndMunicipalityId(String maintenanceStatus, UUID municipalityId);

    @Query("SELECT * FROM maintenances WHERE scheduled_date BETWEEN :startDate AND :endDate")
    Flux<MaintenanceEntity> findByScheduledDateBetween(LocalDate startDate, LocalDate endDate);
    @Query("SELECT COUNT(*) FROM maintenances WHERE DATE(created_at) = CURRENT_DATE")
    Mono<Long> countMaintenancesToday();

    @Query("SELECT COUNT(*) FROM maintenances WHERE municipality_id = :municipalityId AND EXTRACT(YEAR FROM created_at) = :year")
    Mono<Long> countByMunicipalityIdAndYear(UUID municipalityId, int year);
}
