package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.PositionService;
import pe.edu.vallegrande.configurationservice.domain.model.Position;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.PositionRepository;
import pe.edu.vallegrande.configurationservice.infrastructure.config.JwtContextHelper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - PositionService")
class PositionServiceTest {

    @Mock
    private PositionRepository repository;

    @Mock
    private JwtContextHelper jwtContextHelper;

    @InjectMocks
    private PositionService positionService;

    private static final UUID MUNICIPALITY_ID = UUID.fromString("24ad12a5-d9e5-4cdd-91f1-8fd0355c9473");
    private UUID positionId;
    private Position positionActivo;
    private Position positionInactivo;

    @BeforeEach
    void setUp() {
        positionId = UUID.randomUUID();

        positionActivo = Position.builder()
                .id(positionId)
                .positionCode("GER-01")
                .name("Gerente")
                .description("Gerente general de la municipalidad")
                .hierarchicalLevel(1)
                .baseSalary(new BigDecimal("5000.00"))
                .active(true)
                .municipalityId(MUNICIPALITY_ID)
                .build();

        positionInactivo = Position.builder()
                .id(positionId)
                .positionCode("AUX-01")
                .name("Auxiliar")
                .description("Auxiliar administrativo")
                .hierarchicalLevel(5)
                .baseSalary(new BigDecimal("1500.00"))
                .active(false)
                .municipalityId(MUNICIPALITY_ID)
                .build();
    }

    @Test
    @DisplayName("Crear cargo exitosamente con datos válidos")
    void crear_cargo_exitosamente_con_datos_validos() {
        Position input = Position.builder()
                .positionCode("GER-01")
                .name("Gerente")
                .hierarchicalLevel(1)
                .baseSalary(new BigDecimal("5000.00"))
                .build();

        when(jwtContextHelper.getMunicipalityId()).thenReturn(Mono.just(MUNICIPALITY_ID));
        when(repository.save(any(Position.class))).thenReturn(Mono.just(positionActivo));

        StepVerifier.create(positionService.create(input))
                .assertNext(result -> {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getPositionCode()).isEqualTo("GER-01");
                    assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
                })
                .verifyComplete();

        verify(repository, times(1)).save(any(Position.class));
    }

    @Test
    @DisplayName("Al crear cargo, municipalityId se toma del JWT")
    void crear_cargo_toma_municipality_id_del_jwt() {
        Position input = Position.builder().positionCode("GER-01").name("Gerente").build();

        when(jwtContextHelper.getMunicipalityId()).thenReturn(Mono.just(MUNICIPALITY_ID));
        when(repository.save(any(Position.class)))
                .thenAnswer(inv -> Mono.just((Position) inv.getArgument(0)));

        StepVerifier.create(positionService.create(input))
                .assertNext(result -> {
                    assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Lanza error al intentar editar un cargo inactivo")
    void actualizar_cargo_lanza_excepcion_cuando_cargo_esta_inactivo() {
        Position nuevosDatos = Position.builder().name("Auxiliar Senior").build();

        when(repository.findById(positionId)).thenReturn(Mono.just(positionInactivo));

        StepVerifier.create(positionService.update(positionId, nuevosDatos))
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                        error.getMessage().equals("Cannot edit an inactive position"))
                .verify();

        verify(repository, never()).save(any(Position.class));
    }

    @Test
    @DisplayName("Actualizar cargo activo exitosamente")
    void actualizar_cargo_activo_exitosamente() {
        Position nuevosDatos = Position.builder()
                .name("Gerente General")
                .baseSalary(new BigDecimal("6000.00"))
                .build();

        Position actualizado = Position.builder()
                .id(positionId).positionCode("GER-01")
                .name("Gerente General").baseSalary(new BigDecimal("6000.00"))
                .active(true).municipalityId(MUNICIPALITY_ID).build();

        when(repository.findById(positionId)).thenReturn(Mono.just(positionActivo));
        when(repository.save(any(Position.class))).thenReturn(Mono.just(actualizado));

        StepVerifier.create(positionService.update(positionId, nuevosDatos))
                .assertNext(result -> {
                    assertThat(result.getName()).isEqualTo("Gerente General");
                    assertThat(result.getBaseSalary()).isEqualByComparingTo("6000.00");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Soft delete cambia active a false")
    void soft_delete_cambia_active_a_false() {
        Position desactivado = Position.builder().id(positionId).active(false).build();

        when(repository.findById(positionId)).thenReturn(Mono.just(positionActivo));
        when(repository.save(any(Position.class))).thenReturn(Mono.just(desactivado));

        StepVerifier.create(positionService.softDelete(positionId))
                .assertNext(result -> assertThat(result.getActive()).isFalse())
                .verifyComplete();
    }

    @Test
    @DisplayName("Restaurar cargo inactivo cambia active a true")
    void restaurar_cargo_inactivo_cambia_active_a_true() {
        Position restaurado = Position.builder().id(positionId).active(true).build();

        when(repository.findById(positionId)).thenReturn(Mono.just(positionInactivo));
        when(repository.save(any(Position.class))).thenReturn(Mono.just(restaurado));

        StepVerifier.create(positionService.restore(positionId))
                .assertNext(result -> assertThat(result.getActive()).isTrue())
                .verifyComplete();
    }
}
