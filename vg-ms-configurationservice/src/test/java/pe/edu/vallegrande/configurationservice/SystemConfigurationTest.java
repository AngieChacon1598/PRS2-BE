package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.SystemConfigurationService;
import pe.edu.vallegrande.configurationservice.domain.model.SystemConfiguration;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.SystemConfigurationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - SystemConfigurationService")
class SystemConfigurationTest {

    @Mock
    private SystemConfigurationRepository repository;

    @InjectMocks
    private SystemConfigurationService service;

    private UUID configId;
    private SystemConfiguration configBase;

    @BeforeEach
    void setUp() {
        configId = UUID.randomUUID();

        configBase = SystemConfiguration.builder()
                .id(configId)
                .category("GENERAL")
                .key("app.name")
                .value("MiApp")
                .isEditable(true)
                .requiresRestart(false)
                .isSensitive(false)
                .build();
    }

    @Test
    @DisplayName("getAll retorna lista ordenada por categoría")
    void getAll_retorna_lista_ordenada_por_categoria() {

        SystemConfiguration c1 = SystemConfiguration.builder().category("ZETA").build();
        SystemConfiguration c2 = SystemConfiguration.builder().category("ALPHA").build();

        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(c1, c2)));

        StepVerifier.create(service.getAll())
                .expectNextMatches(c -> c.getCategory().equals("ALPHA"))
                .expectNextMatches(c -> c.getCategory().equals("ZETA"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Crear configuración asigna valores por defecto correctamente")
    void crear_configuracion_asigna_valores_por_defecto() {

        SystemConfiguration input = SystemConfiguration.builder()
                .category("GENERAL")
                .key("app.name")
                .value("MiApp")
                .build();

        when(repository.save(any(SystemConfiguration.class)))
                .thenReturn(Mono.just(configBase));

        StepVerifier.create(service.create(input))
                .assertNext(result -> {
                    assertThat(result.getCategory()).isEqualTo("GENERAL");
                    assertThat(result.getIsEditable()).isTrue();
                    assertThat(result.getRequiresRestart()).isFalse();
                    assertThat(result.getIsSensitive()).isFalse();
                })
                .verifyComplete();

        verify(repository).save(any(SystemConfiguration.class));
    }

    @Test
    @DisplayName("Update modifica datos correctamente cuando existe")
    void update_modifica_datos_cuando_existe() {

        SystemConfiguration nuevosDatos = SystemConfiguration.builder()
                .category("SECURITY")
                .key("auth.enabled")
                .value("true")
                .isEditable(false)
                .build();

        SystemConfiguration actualizado = SystemConfiguration.builder()
                .id(configId)
                .category("SECURITY")
                .key("auth.enabled")
                .value("true")
                .isEditable(false)
                .build();

        when(repository.findById(configId)).thenReturn(Mono.just(configBase));
        when(repository.save(any(SystemConfiguration.class))).thenReturn(Mono.just(actualizado));

        StepVerifier.create(service.update(configId, nuevosDatos))
                .assertNext(result -> {
                    assertThat(result.getCategory()).isEqualTo("SECURITY");
                    assertThat(result.getKey()).isEqualTo("auth.enabled");
                    assertThat(result.getValue()).isEqualTo("true");
                })
                .verifyComplete();

        verify(repository).save(any(SystemConfiguration.class));
    }

    @Test
    @DisplayName("Update no ejecuta save cuando no existe")
    void update_no_ejecuta_save_cuando_no_existe() {

        SystemConfiguration nuevosDatos = SystemConfiguration.builder()
                .category("TEST")
                .build();

        when(repository.findById(configId)).thenReturn(Mono.empty());

        StepVerifier.create(service.update(configId, nuevosDatos))
                .verifyComplete();

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Soft delete desactiva edición (isEditable = false)")
    void soft_delete_desactiva_edicion() {

        SystemConfiguration desactivada = SystemConfiguration.builder()
                .id(configId)
                .isEditable(false)
                .build();

        when(repository.findById(configId)).thenReturn(Mono.just(configBase));
        when(repository.save(any(SystemConfiguration.class))).thenReturn(Mono.just(desactivada));

        StepVerifier.create(service.softDelete(configId))
                .verifyComplete();

        verify(repository).save(any(SystemConfiguration.class));
    }

    @Test
    @DisplayName("Restore activa edición (isEditable = true)")
    void restore_activa_edicion() {

        SystemConfiguration inactiva = SystemConfiguration.builder()
                .id(configId)
                .isEditable(false)
                .build();

        SystemConfiguration restaurada = SystemConfiguration.builder()
                .id(configId)
                .isEditable(true)
                .build();

        when(repository.findById(configId)).thenReturn(Mono.just(inactiva));
        when(repository.save(any(SystemConfiguration.class))).thenReturn(Mono.just(restaurada));

        StepVerifier.create(service.restore(configId))
                .verifyComplete();

        verify(repository).save(any(SystemConfiguration.class));
    }
}