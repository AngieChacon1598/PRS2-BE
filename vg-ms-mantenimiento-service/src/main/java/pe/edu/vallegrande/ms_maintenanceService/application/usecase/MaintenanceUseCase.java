package pe.edu.vallegrande.ms_maintenanceService.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_maintenanceService.application.mapper.MaintenanceMapper;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceNotFoundException;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceValidationException;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceStatusHistory;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.in.MaintenanceServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.MaintenanceRepositoryPort;
import pe.edu.vallegrande.ms_maintenanceService.domain.service.MaintenanceValidator;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalAssetServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalTenantServicePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceUseCase implements MaintenanceServicePort {

    private static final DateTimeFormatter WORK_ORDER_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final MaintenanceRepositoryPort repositoryPort;
    private final MaintenanceMapper mapper;
    private final MaintenanceValidator validator;
    private final ExternalTenantServicePort tenantService;
    private final ExternalAssetServicePort assetService;

    @Override
    public Mono<Maintenance> create(Maintenance maintenance) {
        log.info("Creando mantenimiento para municipio: {} y bien: {}", maintenance.getMunicipalityId(),
                maintenance.getAssetId());
        validateWarranty(maintenance.getHasWarranty(), maintenance.getWarrantyExpirationDate());
        validator.validateCreation(maintenance);

        maintenance.setMaintenanceStatus("SCHEDULED");
        maintenance.setLaborCost(maintenance.getLaborCost() != null ? maintenance.getLaborCost() : BigDecimal.ZERO);
        maintenance.setPartsCost(BigDecimal.ZERO);
        maintenance.setAdditionalCost(
                maintenance.getAdditionalCost() != null ? maintenance.getAdditionalCost() : BigDecimal.ZERO);
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setUpdatedAt(LocalDateTime.now());

        int currentYear = LocalDate.now().getYear();

        // 1. Obtener detalles del Bien (Patrimonio)
        // 2. Obtener Ubigeo (Tenant)
        // 3. Obtener Correlativo y Generar Código
        return assetService.fillAssetDetails(maintenance)
                .flatMap(m -> Mono.zip(
                        tenantService.getUbigeoCodeByMunicipalityId(m.getMunicipalityId()),
                        repositoryPort.countMaintenancesByMunicipalityAndYear(m.getMunicipalityId(), currentYear))
                        .flatMap(tuple -> {
                            String ubigeo = tuple.getT1();
                            long count = tuple.getT2();
                            String typeCode = getMaintenanceTypeCode(m.getMaintenanceType());
                            String generatedCode = String.format("MNT-%s-%s-%d-%06d", ubigeo, typeCode, currentYear,
                                    count + 1);

                            m.setMaintenanceCode(generatedCode);
                            return repositoryPort.save(m);
                        }))
                .flatMap(
                        saved -> saveStatusHistory(saved, null, "SCHEDULED", "Creación inicial", saved.getRequestedBy())
                                .thenReturn(saved));
    }

    @Override
    public Mono<Maintenance> findById(UUID id) {
        return repositoryPort.findById(id)
                .switchIfEmpty(
                        Mono.error(new MaintenanceNotFoundException("Mantenimiento no encontrado con ID: " + id)));
    }

    @Override
    public Flux<Maintenance> findAll(UUID municipalityId) {
        return repositoryPort.findAllByMunicipalityId(municipalityId);
    }

    @Override
    public Mono<Maintenance> update(UUID id, Maintenance updatedData) {
        return repositoryPort.findById(id)
                .switchIfEmpty(
                        Mono.error(new MaintenanceNotFoundException("Mantenimiento no encontrado con ID: " + id)))
                .flatMap(existing -> {
                    Maintenance updated = mapper.updateEntity(existing, updatedData);
                    return repositoryPort.save(updated);
                });
    }

    @Override
    public Mono<Maintenance> startMaintenance(UUID id, UUID updatedBy, String observations) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "IN_PROCESS");
                    String previous = m.getMaintenanceStatus();
                    m.setMaintenanceStatus("IN_PROCESS");
                    m.setStartDate(LocalDateTime.now());
                    m.setUpdatedAt(LocalDateTime.now());
                    m.setUpdatedBy(updatedBy);
                    return repositoryPort.save(m)
                            .flatMap(saved -> saveStatusHistory(saved, previous, "IN_PROCESS", observations, updatedBy)
                                    .thenReturn(saved));
                });
    }

    @Override
    public Mono<Maintenance> completeMaintenance(UUID id, String workOrder, BigDecimal laborCost,
            String appliedSolution, String observations, UUID updatedBy) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "PENDING_CONFORMITY");
                    validator.validateCompletion(appliedSolution, laborCost);

                    // Verificar que exista al menos un repuesto/parte registrado
                    return repositoryPort.findAllPartsByMaintenanceId(id).count()
                            .flatMap(count -> {
                                if (count == 0) {
                                    return Mono.error(new MaintenanceValidationException(
                                            "Debe registrar al menos un repuesto o detalle de parte en la sección correspondiente antes de marcar como completado."));
                                }

                                String previous = m.getMaintenanceStatus();
                                m.setEndDate(LocalDateTime.now());
                                m.setWorkOrder(resolveWorkOrder(m, workOrder));
                                m.setLaborCost(laborCost);
                                m.setAppliedSolution(appliedSolution);
                                m.setUpdatedBy(updatedBy);
                                m.setUpdatedAt(LocalDateTime.now());
                                m.setMaintenanceStatus("PENDING_CONFORMITY");

                                return repositoryPort.save(m)
                                        .flatMap(saved -> saveStatusHistory(saved, previous, "PENDING_CONFORMITY",
                                                observations,
                                                updatedBy).thenReturn(saved));
                            });
                });
    }

    @Override
    public Mono<Maintenance> confirmMaintenance(UUID id, MaintenanceConformity conformity) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "CONFIRMED");
                    int currentYear = LocalDate.now().getYear();

                    return Mono.zip(
                            tenantService.getUbigeoCodeByMunicipalityId(m.getMunicipalityId()),
                            repositoryPort.countConformitiesByMunicipalityAndYear(m.getMunicipalityId(), currentYear))
                            .flatMap(tuple -> {
                                String ubigeo = tuple.getT1();
                                long count = tuple.getT2();
                                String genCode = String.format("CONF-%s-%d-%06d", ubigeo, currentYear, count + 1);

                                conformity.setConformityNumber(genCode);
                                conformity.setCreatedAt(LocalDateTime.now());

                                String previous = m.getMaintenanceStatus();
                                m.setMaintenanceStatus("CONFIRMED");
                                m.setUpdatedAt(LocalDateTime.now());

                                return repositoryPort.save(m)
                                        .flatMap(saved -> repositoryPort.saveConformity(conformity).thenReturn(saved))
                                        .flatMap(saved -> saveStatusHistory(saved, previous, "CONFIRMED",
                                                "Acta firmada: " + genCode, conformity.getConfirmedBy())
                                                .thenReturn(saved));
                            });
                });
    }

    @Override
    public Mono<MaintenancePart> addPart(UUID maintenanceId, MaintenancePart part) {
        // Garantizar que campos base no sean null para el objeto que se guarda
        if (part.getQuantity() == null) part.setQuantity(BigDecimal.ONE);
        if (part.getUnitPrice() == null) part.setUnitPrice(BigDecimal.ZERO);
        part.setCreatedAt(LocalDateTime.now());

        // Calcular el incremento manualmente ya que savedPart.getSubtotal() puede venir null (columna generada)
        BigDecimal partSubtotal = part.getUnitPrice().multiply(part.getQuantity());

        return repositoryPort.savePart(part)
                .flatMap(savedPart -> repositoryPort.findById(maintenanceId)
                        .flatMap(m -> {
                            BigDecimal currentPartsCost = m.getPartsCost() != null ? m.getPartsCost() : BigDecimal.ZERO;
                            m.setPartsCost(currentPartsCost.add(partSubtotal));
                            // No seteamos totalCost, la DB lo calcula automáticamente al ser GENERATED ALWAYS
                            return repositoryPort.save(m).map(savedM -> {
                                savedPart.setSubtotal(partSubtotal);
                                return savedPart;
                            });
                        }));
    }

    @Override
    public Flux<MaintenancePart> getParts(UUID maintenanceId) {
        return repositoryPort.findAllPartsByMaintenanceId(maintenanceId);
    }

    @Override
    public Flux<MaintenanceStatusHistory> getHistory(UUID maintenanceId) {
        return repositoryPort.findHistoryByMaintenanceId(maintenanceId);
    }

    @Override
    public Mono<Maintenance> suspendMaintenance(UUID id, LocalDate nextDate, String observations, UUID updatedBy) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "SUSPENDED");
                    String previous = m.getMaintenanceStatus();
                    m.setMaintenanceStatus("SUSPENDED");
                    m.setNextDate(nextDate);
                    m.setUpdatedBy(updatedBy);
                    return repositoryPort.save(m)
                            .flatMap(saved -> saveStatusHistory(saved, previous, "SUSPENDED", observations, updatedBy)
                                    .thenReturn(saved));
                });
    }

    @Override
    public Mono<Maintenance> cancelMaintenanceWithReason(UUID id, String observations, UUID updatedBy) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "CANCELLED");
                    String previous = m.getMaintenanceStatus();
                    m.setMaintenanceStatus("CANCELLED");
                    m.setUpdatedBy(updatedBy);
                    return repositoryPort.save(m)
                            .flatMap(saved -> saveStatusHistory(saved, previous, "CANCELLED", observations, updatedBy)
                                    .thenReturn(saved));
                });
    }

    @Override
    public Mono<Maintenance> rescheduleMaintenance(UUID id, LocalDate nextDate, UUID technicalResponsibleId,
            UUID serviceSupplierId, String observations, UUID updatedBy) {
        return repositoryPort.findById(id)
                .flatMap(m -> {
                    validator.validateStateTransition(m.getMaintenanceStatus(), "SCHEDULED");
                    String previous = m.getMaintenanceStatus();
                    m.setMaintenanceStatus("SCHEDULED");
                    m.setScheduledDate(nextDate);
                    if (technicalResponsibleId != null)
                        m.setTechnicalResponsibleId(technicalResponsibleId);
                    if (serviceSupplierId != null)
                        m.setServiceSupplierId(serviceSupplierId);
                    return repositoryPort.save(m)
                            .flatMap(saved -> saveStatusHistory(saved, previous, "SCHEDULED", observations, updatedBy)
                                    .thenReturn(saved));
                });
    }

    @Override
    public Flux<Maintenance> findByStatus(String status, UUID municipalityId) {
        return repositoryPort.findByMaintenanceStatusAndMunicipalityId(status, municipalityId);
    }

    private Mono<MaintenanceStatusHistory> saveStatusHistory(Maintenance m, String prev, String next, String reason,
            UUID user) {
        return repositoryPort.saveHistory(MaintenanceStatusHistory.builder()
                .maintenanceId(m.getId())
                .municipalityId(m.getMunicipalityId())
                .previousStatus(prev)
                .newStatus(next)
                .reason(reason)
                .changedBy(user)
                .changedAt(LocalDateTime.now())
                .build());
    }

    private void validateWarranty(Boolean hasWarranty, LocalDate warrantyExpirationDate) {
        if (Boolean.TRUE.equals(hasWarranty) && warrantyExpirationDate == null) {
            throw new MaintenanceValidationException("Debe proporcionar la fecha de expiración para la garantía.");
        }
    }

    private String resolveWorkOrder(Maintenance maintenance, String requestedWorkOrder) {
        if (requestedWorkOrder != null && !requestedWorkOrder.isBlank()) {
            return requestedWorkOrder.trim();
        }
        String identifier = maintenance.getId().toString().substring(0, 8).toUpperCase();
        return "WO-" + LocalDate.now().format(WORK_ORDER_DATE_FORMAT) + "-" + identifier;
    }

    private String getMaintenanceTypeCode(String type) {
        switch (type != null ? type : "") {
            case "PREVENTIVE":
                return "PRV";
            case "CORRECTIVE":
                return "COR";
            case "PREDICTIVE":
                return "PRE";
            case "EMERGENCY":
                return "EME";
            default:
                return "OTH";
        }
    }
}
