package pe.edu.vallegrande.ms_maintenanceService.domain.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceStatusHistory;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceServicePort {

    Mono<Maintenance> create(Maintenance maintenance);
    Mono<Maintenance> findById(UUID id);
    Flux<Maintenance> findAll(UUID municipalityId);
    Mono<Maintenance> update(UUID id, Maintenance maintenance);
    Flux<Maintenance> findByStatus(String status, UUID municipalityId);

    // Flujo de Estados SBN
    Mono<Maintenance> startMaintenance(UUID id, UUID updatedBy, String observations);
    
    Mono<Maintenance> completeMaintenance(UUID id, String workOrder,
            BigDecimal laborCost, String appliedSolution, 
            String observations, UUID updatedBy);
            
    Mono<Maintenance> confirmMaintenance(UUID id, MaintenanceConformity conformity);

    Mono<Maintenance> suspendMaintenance(UUID id, LocalDate nextDate,
            String observations, UUID updatedBy);

    Mono<Maintenance> cancelMaintenanceWithReason(UUID id, String observations, UUID updatedBy);

    Mono<Maintenance> rescheduleMaintenance(UUID id, LocalDate nextDate,
            UUID technicalResponsibleId, UUID serviceSupplierId,
            String observations, UUID updatedBy);

    // Gestión de Detalles
    Mono<MaintenancePart> addPart(UUID maintenanceId, MaintenancePart part);
    Flux<MaintenancePart> getParts(UUID maintenanceId);
    Flux<MaintenanceStatusHistory> getHistory(UUID maintenanceId);
}

