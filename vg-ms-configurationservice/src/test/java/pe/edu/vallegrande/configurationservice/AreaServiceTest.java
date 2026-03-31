package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.AreaService;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.domain.model.Area;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.AreaRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - AreaService")
class AreaServiceTest {

    @Mock
    private AreaRepository repository;

    @InjectMocks
    private AreaService areaService;

    private UUID areaId;
    private Area areaActiva;

    @BeforeEach
    void setUp() {
        areaId = UUID.randomUUID();
        areaActiva = Area.builder()
                .id(areaId)
                .areaCode("ADM-01")
                .name("Administración")
                .active(true)
                .municipalityId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("Crear área establece active en true y timestamps")
    void crear_area_establece_active_y_timestamps() {
        Area input = Area.builder().areaCode("ADM-01").name("Administración").build();

        when(repository.save(any(Area.class))).thenReturn(Mono.just(areaActiva));

        StepVerifier.create(areaService.create(input))
                .assertNext(result -> {
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getAreaCode()).isEqualTo("ADM-01");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getById retorna 404 cuando el área no existe")
    void getById_retorna_404_cuando_no_existe() {
        when(repository.findById(areaId)).thenReturn(Mono.empty());

        StepVerifier.create(areaService.getById(areaId))
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                        error.getMessage().contains(areaId.toString()))
                .verify();
    }

    @Test
    @DisplayName("getById retorna el área cuando existe")
    void getById_retorna_area_cuando_existe() {
        when(repository.findById(areaId)).thenReturn(Mono.just(areaActiva));

        StepVerifier.create(areaService.getById(areaId))
                .assertNext(result -> assertThat(result.getId()).isEqualTo(areaId))
                .verifyComplete();
    }

    @Test
    @DisplayName("Soft delete cambia active a false")
    void soft_delete_cambia_active_a_false() {
        Area desactivada = Area.builder().id(areaId).active(false).build();

        when(repository.findById(areaId)).thenReturn(Mono.just(areaActiva));
        when(repository.save(any(Area.class))).thenReturn(Mono.just(desactivada));

        StepVerifier.create(areaService.softDelete(areaId))
                .verifyComplete();

        verify(repository, times(1)).save(any(Area.class));
    }

    @Test
    @DisplayName("Restaurar área cambia active a true")
    void restaurar_area_cambia_active_a_true() {
        Area inactiva = Area.builder().id(areaId).active(false).build();
        Area restaurada = Area.builder().id(areaId).active(true).build();

        when(repository.findById(areaId)).thenReturn(Mono.just(inactiva));
        when(repository.save(any(Area.class))).thenReturn(Mono.just(restaurada));

        StepVerifier.create(areaService.restore(areaId))
                .verifyComplete();

        verify(repository, times(1)).save(any(Area.class));
    }
}
