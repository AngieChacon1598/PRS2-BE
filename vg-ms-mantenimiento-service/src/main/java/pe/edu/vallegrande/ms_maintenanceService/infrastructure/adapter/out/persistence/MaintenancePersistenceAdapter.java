package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceStatusHistory;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.MaintenanceRepositoryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MaintenancePersistenceAdapter implements MaintenanceRepositoryPort {

    private final MaintenanceR2dbcRepository r2dbcRepository;
    private final MaintenancePartR2dbcRepository partRepository;
    private final MaintenanceStatusHistoryR2dbcRepository historyRepository;
    private final MaintenanceConformityR2dbcRepository conformityRepository;
    private final MaintenancePersistenceMapper persistenceMapper;

    @Override
    public Mono<Maintenance> save(Maintenance maintenance) {
        MaintenanceEntity entity = persistenceMapper.toEntity(maintenance);
        return r2dbcRepository.save(entity)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Mono<Maintenance> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Flux<Maintenance> findAllByMunicipalityId(UUID municipalityId) {
        return r2dbcRepository.findByMunicipalityId(municipalityId)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Flux<Maintenance> findAll() {
        return r2dbcRepository.findAll()
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Mono<Maintenance> findByMaintenanceCode(String maintenanceCode) {
        return r2dbcRepository.findByMaintenanceCode(maintenanceCode)
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Flux<Maintenance> findByMaintenanceStatusAndMunicipalityId(String maintenanceStatus, UUID municipalityId) {
        return r2dbcRepository.findByMaintenanceStatusAndMunicipalityId(maintenanceStatus, municipalityId)
                .map(persistenceMapper::toDomain);
    }

    // Implementaciones para Detalle de Repuestos

    @Override
    public Mono<MaintenancePart> savePart(MaintenancePart part) {
        return partRepository.save(persistenceMapper.toEntity(part))
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Flux<MaintenancePart> findAllPartsByMaintenanceId(UUID maintenanceId) {
        return partRepository.findByMaintenanceId(maintenanceId)
                .map(persistenceMapper::toDomain);
    }

    // Implementaciones para Historial de Estados

    @Override
    public Mono<MaintenanceStatusHistory> saveHistory(MaintenanceStatusHistory history) {
        return historyRepository.save(persistenceMapper.toEntity(history))
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Flux<MaintenanceStatusHistory> findHistoryByMaintenanceId(UUID maintenanceId) {
        return historyRepository.findByMaintenanceIdOrderByChangedAtDesc(maintenanceId)
                .map(persistenceMapper::toDomain);
    }

    // Implementaciones para Acta de Conformidad

    @Override
    public Mono<MaintenanceConformity> saveConformity(MaintenanceConformity conformity) {
        return conformityRepository.save(persistenceMapper.toEntity(conformity))
                .map(persistenceMapper::toDomain);
    }

    @Override
    public Mono<MaintenanceConformity> findConformityByMaintenanceId(UUID maintenanceId) {
        return conformityRepository.findByMaintenanceId(maintenanceId)
                .map(persistenceMapper::toDomain);
    }
    @Override
    public Mono<Long> countMaintenancesToday() {
        return r2dbcRepository.countMaintenancesToday();
    }

    @Override
    public Mono<Long> countMaintenancesByMunicipalityAndYear(UUID municipalityId, int year) {
        return r2dbcRepository.countByMunicipalityIdAndYear(municipalityId, year);
    }

    @Override
    public Mono<Long> countConformitiesByMunicipalityAndYear(UUID municipalityId, int year) {
        return conformityRepository.countByMunicipalityIdAndYear(municipalityId, year);
    }
}
