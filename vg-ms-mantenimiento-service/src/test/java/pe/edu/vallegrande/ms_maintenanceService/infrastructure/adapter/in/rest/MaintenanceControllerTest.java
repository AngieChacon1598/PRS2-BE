package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import pe.edu.vallegrande.ms_maintenanceService.application.dto.*;
import pe.edu.vallegrande.ms_maintenanceService.application.mapper.MaintenanceMapper;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.in.MaintenanceServicePort;
import pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest.dto.MaintenanceWebRequest;
import pe.edu.vallegrande.ms_maintenanceService.infrastructure.config.SecurityConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = MaintenanceController.class)
@Import(SecurityConfig.class)
public class MaintenanceControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private MaintenanceServicePort maintenanceService;

    @MockitoBean
    private MaintenanceMapper mapper;

    @MockitoBean
    private pe.edu.vallegrande.ms_maintenanceService.infrastructure.config.JwtAuthenticationConverter jwtAuthenticationConverter;

    private UUID maintenanceId;
    private MaintenanceResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        maintenanceId = UUID.randomUUID();
        responseDTO = MaintenanceResponseDTO.builder()
                .id(maintenanceId)
                .maintenanceCode("MT-2026-001")
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .maintenanceStatus("SCHEDULED")
                .build();

        // Stubbing the converter mock to return an Authentication with authorities
        when(jwtAuthenticationConverter.convert(any())).thenAnswer(invocation -> {
            org.springframework.security.oauth2.jwt.Jwt jwt = invocation.getArgument(0);
            return Mono.just(new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
                jwt, 
                org.springframework.security.core.authority.AuthorityUtils.createAuthorityList(
                    "mantenimiento:read", "mantenimiento:create", "mantenimiento:update", 
                    "mantenimiento:close", "mantenimiento:confirm", "ROLE_TENANT_ADMIN"
                )
            ));
        });
    }

    @Test
    void buscarPorId_Autorizado_RetornaMantenimiento() {
        when(maintenanceService.findById(maintenanceId)).thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).build()));
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:read"))
                .jwt(jwt -> jwt.claim("municipal_code", UUID.randomUUID().toString())))
                .get()
                .uri("/api/v1/maintenances/{id}", maintenanceId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(maintenanceId.toString());
    }

    @Test
    void crear_Autorizado_RetornaCreado() {
        MaintenanceWebRequest request = MaintenanceWebRequest.builder()
                .maintenanceCode("MT-2026-002")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("MEDIUM")
                .scheduledDate(LocalDate.now())
                .workDescription("Desc")
                .reportedProblem("Prob")
                .requestedBy(UUID.randomUUID())
                .technicalResponsibleId(UUID.randomUUID())
                .hasWarranty(false)
                .build();

        when(mapper.toEntity(any(MaintenanceRequestDTO.class))).thenReturn(Maintenance.builder().build());
        when(maintenanceService.create(any())).thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).build()));
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:create"))
                .jwt(jwt -> jwt.claim("municipal_code", UUID.randomUUID().toString())
                               .claim("user_id", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void registrarRepuesto_Autorizado_RetornaCreado() {
        MaintenancePartRequest request = MaintenancePartRequest.builder()
                .partName("Batería")
                .partType("SPARE_PART")
                .quantity(new BigDecimal("1"))
                .unitCost(new BigDecimal("450"))
                .build();

        when(maintenanceService.addPart(any(), any())).thenReturn(Mono.just(MaintenancePart.builder().build()));

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:update"))
                .jwt(jwt -> jwt.claim("municipal_code", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances/{id}/parts", maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void obtenerHistorial_Autorizado_RetornaLista() {
        when(maintenanceService.getHistory(maintenanceId)).thenReturn(Flux.empty());

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:read"))
                .jwt(jwt -> jwt.claim("municipal_code", UUID.randomUUID().toString())))
                .get()
                .uri("/api/v1/maintenances/{id}/history", maintenanceId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void completarMantenimiento_Autorizado_RetornaOk() {
        CompleteMaintenanceRequest request = CompleteMaintenanceRequest.builder()
                .workOrder("WO-2026-X")
                .laborCost(new BigDecimal("250.00"))
                .appliedSolution("Limpieza profunda")
                .observations(null)
                .updatedBy(UUID.randomUUID())
                .build();

        when(maintenanceService.completeMaintenance(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).build()));
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:close"))
                .jwt(jwt -> jwt.claim("user_id", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances/{id}/complete", maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void confirmarMantenimiento_Autorizado_RetornaMantenimiento() {
        MaintenanceConformityRequest request = MaintenanceConformityRequest.builder()
                .conformityNumber("CONF-2026-X")
                .workQuality("EXCELLENT")
                .assetConditionAfter("OPERATIONAL")
                .observations("Bien recibido y en óptimas condiciones")
                .confirmedBy(UUID.randomUUID())
                .build();

        when(maintenanceService.confirmMaintenance(any(), any()))
                .thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).maintenanceStatus("CONFIRMED").build()));
        when(mapper.toResponseDTO(any())).thenReturn(MaintenanceResponseDTO.builder()
                .id(maintenanceId)
                .maintenanceStatus("CONFIRMED")
                .build());

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:confirm"))
                .jwt(jwt -> jwt.claim("municipal_code", UUID.randomUUID().toString())
                               .claim("user_id", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances/{id}/confirm", maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.maintenanceStatus").isEqualTo("CONFIRMED");
    }

    @Test
    void suspenderMantenimiento_Autorizado_RetornaOk() {
        SuspendMaintenanceRequest request = new SuspendMaintenanceRequest();
        request.setNextDate(LocalDate.now().plusWeeks(2));
        request.setObservations("Esperando repuestos críticos");
        request.setUpdatedBy(UUID.randomUUID());

        when(maintenanceService.suspendMaintenance(any(), any(), any(), any()))
                .thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).maintenanceStatus("SUSPENDED").build()));
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:update"))
                .jwt(jwt -> jwt.claim("user_id", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances/{id}/suspend", maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cancelarMantenimiento_Autorizado_RetornaOk() {
        CancelMaintenanceRequest request = new CancelMaintenanceRequest();
        request.setObservations("Bien ya no se encuentra en el local");
        request.setUpdatedBy(UUID.randomUUID());

        when(maintenanceService.cancelMaintenanceWithReason(any(), any(), any()))
                .thenReturn(Mono.just(Maintenance.builder().id(maintenanceId).maintenanceStatus("CANCELLED").build()));
        when(mapper.toResponseDTO(any())).thenReturn(responseDTO);

        webTestClient.mutateWith(mockJwt()
                .authorities(new SimpleGrantedAuthority("mantenimiento:update"))
                .jwt(jwt -> jwt.claim("user_id", UUID.randomUUID().toString())))
                .post()
                .uri("/api/v1/maintenances/{id}/cancel", maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }
}
