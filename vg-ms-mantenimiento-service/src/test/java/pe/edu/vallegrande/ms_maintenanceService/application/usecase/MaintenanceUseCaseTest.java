package pe.edu.vallegrande.ms_maintenanceService.application.usecase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.ms_maintenanceService.application.mapper.MaintenanceMapper;
import pe.edu.vallegrande.ms_maintenanceService.domain.exception.MaintenanceValidationException;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalAssetServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalTenantServicePort;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.MaintenanceRepositoryPort;
import pe.edu.vallegrande.ms_maintenanceService.domain.service.MaintenanceValidator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class MaintenanceUseCaseTest {

    @Mock
    private MaintenanceRepositoryPort repositoryPort;

    @Mock
    private ExternalAssetServicePort assetService;

    @Mock
    private ExternalTenantServicePort tenantService;

    private MaintenanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MaintenanceUseCase(repositoryPort, new MaintenanceMapper(), new MaintenanceValidator(), tenantService, assetService);
    }

    @Test
    void crearMantenimiento_Valido_IniciaEnProgramado() {
        Maintenance maintenance = buildMaintenance("SCHEDULED");
        when(assetService.fillAssetDetails(any())).thenReturn(Mono.just(maintenance));
        when(tenantService.getUbigeoCodeByMunicipalityId(any())).thenReturn(Mono.just("150101"));
        when(repositoryPort.countMaintenancesByMunicipalityAndYear(any(), anyInt())).thenReturn(Mono.just(0L));
        when(repositoryPort.save(any())).thenReturn(Mono.just(maintenance));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(maintenance))
                .assertNext(result -> {
                    assertEquals("SCHEDULED", result.getMaintenanceStatus());
                    verify(repositoryPort).saveHistory(any());
                })
                .verifyComplete();
    }

    @Test
    void iniciarMantenimiento_CambiaAEnProceso() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("SCHEDULED");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenReturn(Mono.just(maintenance));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.startMaintenance(id, UUID.randomUUID(), "Iniciando"))
                .assertNext(result -> {
                    assertEquals("IN_PROCESS", result.getMaintenanceStatus());
                    assertNotNull(result.getStartDate());
                })
                .verifyComplete();
    }

    @Test
    void completarMantenimiento_CambiaAPendienteConformidad_YRegistraAudit() {
        UUID id = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenReturn(Mono.just(maintenance));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier
                .create(useCase.completeMaintenance(id, "WO-TEST-001", new BigDecimal("150.00"), 
                        "Motor reparado", "Sin observaciones", user))
                .assertNext(result -> {
                    assertEquals("PENDING_CONFORMITY", result.getMaintenanceStatus());
                    assertEquals("WO-TEST-001", result.getWorkOrder());
                    assertEquals(new BigDecimal("150.00"), result.getLaborCost());
                    assertNotNull(result.getEndDate());
                    assertEquals(user, result.getUpdatedBy());
                    verify(repositoryPort).saveHistory(argThat(h -> 
                        "IN_PROCESS".equals(h.getPreviousStatus()) && 
                        "PENDING_CONFORMITY".equals(h.getNewStatus()) &&
                        "Sin observaciones".equals(h.getReason())
                    ));
                })
                .verifyComplete();
    }

    @Test
    void completarMantenimiento_GeneraWorkOrder_SiNoSeProvee() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier
                .create(useCase.completeMaintenance(id, null, BigDecimal.ZERO, "Test", null, UUID.randomUUID()))
                .assertNext(result -> {
                    assertNotNull(result.getWorkOrder());
                    assert(result.getWorkOrder().startsWith("WO-"));
                })
                .verifyComplete();
    }

    @Test
    void agregarRepuesto_ActualizaCostoDeMantenimiento() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);
        maintenance.setPartsCost(BigDecimal.ZERO);

        MaintenancePart part = MaintenancePart.builder()
                .quantity(new BigDecimal("2"))
                .unitPrice(new BigDecimal("50"))
                .subtotal(new BigDecimal("100"))
                .build();

        when(repositoryPort.savePart(any())).thenReturn(Mono.just(part));
        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenReturn(Mono.just(maintenance));

        StepVerifier.create(useCase.addPart(id, part))
                .assertNext(result -> {
                    assertEquals(new BigDecimal("100"), maintenance.getPartsCost());
                })
                .verifyComplete();
    }

    @Test
    void confirmarMantenimiento_CambiaAConfirmado_YGeneraCodigo() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("PENDING_CONFORMITY");
        maintenance.setId(id);

        MaintenanceConformity conformity = MaintenanceConformity.builder()
                .workQuality("EXCELLENT")
                .confirmedBy(UUID.randomUUID())
                .build();

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(tenantService.getUbigeoCodeByMunicipalityId(any())).thenReturn(Mono.just("150101"));
        when(repositoryPort.countConformitiesByMunicipalityAndYear(any(), anyInt())).thenReturn(Mono.just(5L));
        when(repositoryPort.save(any())).thenReturn(Mono.just(maintenance));
        when(repositoryPort.saveConformity(any())).thenReturn(Mono.empty());
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.confirmMaintenance(id, conformity))
                .assertNext(result -> {
                    assertEquals("CONFIRMED", result.getMaintenanceStatus());
                    verify(repositoryPort).saveConformity(argThat(c -> 
                        c.getConformityNumber().startsWith("CONF-150101-") &&
                        c.getConformityNumber().endsWith("000006")
                    ));
                    verify(repositoryPort).saveHistory(argThat(h -> 
                        h.getNewStatus().equals("CONFIRMED") && 
                        h.getReason().contains("Acta firmada:")
                    ));
                })
                .verifyComplete();
    }

    @Test
    void transicionInvalida_LanzaExcepcion() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("SCHEDULED");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));

        StepVerifier.create(useCase.completeMaintenance(id, "WO-1", BigDecimal.TEN, "Sol", "Obs", UUID.randomUUID()))
                .expectError(MaintenanceValidationException.class)
                .verify();
    }

    @Test
    void completarMantenimiento_Error_SolucionVacia() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));

        StepVerifier.create(useCase.completeMaintenance(id, "WO-1", BigDecimal.TEN, " ", "Obs", UUID.randomUUID()))
                .expectError(MaintenanceValidationException.class)
                .verify();
    }

    @Test
    void suspenderMantenimiento_Exito() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.suspendMaintenance(id, LocalDate.now().plusWeeks(1), "Falta repuesto", UUID.randomUUID()))
                .assertNext(result -> {
                    assertEquals("SUSPENDED", result.getMaintenanceStatus());
                    assertNotNull(result.getNextDate());
                })
                .verifyComplete();
    }

    @Test
    void suspenderMantenimiento_Error_SinMotivo() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("IN_PROCESS");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));

        StepVerifier.create(useCase.suspendMaintenance(id, LocalDate.now().plusWeeks(1), " ", UUID.randomUUID()))
                .expectError(MaintenanceValidationException.class)
                .verify();
    }

    @Test
    void cancelarMantenimiento_Exito() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("SCHEDULED");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));
        when(repositoryPort.save(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repositoryPort.saveHistory(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.cancelMaintenanceWithReason(id, "Ya no es necesario", UUID.randomUUID()))
                .assertNext(result -> {
                    assertEquals("CANCELLED", result.getMaintenanceStatus());
                })
                .verifyComplete();
    }

    @Test
    void cancelarMantenimiento_Error_SinJustificacion() {
        UUID id = UUID.randomUUID();
        Maintenance maintenance = buildMaintenance("SCHEDULED");
        maintenance.setId(id);

        when(repositoryPort.findById(id)).thenReturn(Mono.just(maintenance));

        StepVerifier.create(useCase.cancelMaintenanceWithReason(id, "", UUID.randomUUID()))
                .expectError(MaintenanceValidationException.class)
                .verify();
    }

    private Maintenance buildMaintenance(String status) {
        return Maintenance.builder()
                .municipalityId(UUID.randomUUID())
                .maintenanceCode("MT-TEST")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(LocalDate.now().plusDays(1))
                .workDescription("Desc")
                .reportedProblem("Prob")
                .technicalResponsibleId(UUID.randomUUID())
                .requestedBy(UUID.randomUUID())
                .maintenanceStatus(status)
                .partsCost(BigDecimal.ZERO)
                .hasWarranty(false)
                .build();
    }
}