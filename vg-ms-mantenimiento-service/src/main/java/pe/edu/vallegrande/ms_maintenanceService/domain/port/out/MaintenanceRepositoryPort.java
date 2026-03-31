package pe.edu.vallegrande.ms_maintenanceService.domain.port.out;

import java.util.UUID;

import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceStatusHistory;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceRepositoryPort {

    Mono<Maintenance> save(Maintenance maintenance);
    Mono<Maintenance> findById(UUID id);
    Flux<Maintenance> findAllByMunicipalityId(UUID municipalityId);
    Flux<Maintenance> findAll();
    Mono<Maintenance> findByMaintenanceCode(String maintenanceCode);
    Flux<Maintenance> findByMaintenanceStatusAndMunicipalityId(String maintenanceStatus, UUID municipalityId);

    // Métodos para Detalle de Repuestos
    Mono<MaintenancePart> savePart(MaintenancePart part);
    Flux<MaintenancePart> findAllPartsByMaintenanceId(UUID maintenanceId);

    // Métodos para Historial de Estados
    Mono<MaintenanceStatusHistory> saveHistory(MaintenanceStatusHistory history);
    Flux<MaintenanceStatusHistory> findHistoryByMaintenanceId(UUID maintenanceId);

    // Métodos para Acta de Conformidad
    Mono<MaintenanceConformity> saveConformity(MaintenanceConformity conformity);
    Mono<MaintenanceConformity> findConformityByMaintenanceId(UUID maintenanceId);
    Mono<Long> countMaintenancesToday();
    Mono<Long> countMaintenancesByMunicipalityAndYear(UUID municipalityId, int year);
    Mono<Long> countConformitiesByMunicipalityAndYear(UUID municipalityId, int year);
}
