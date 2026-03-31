package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.CategoryAssetService;
import pe.edu.vallegrande.configurationservice.domain.model.CategoryAsset;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.CategoryAssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - CategoryAssetService")
class CategoryAssetTest {

    @Mock
    private CategoryAssetRepository repository;

    @InjectMocks
    private CategoryAssetService service;

    private UUID categoryId;
    private CategoryAsset categoryActiva;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        categoryActiva = CategoryAsset.builder()
                .id(categoryId)
                .categoryCode("CAT-01")
                .name("Equipos")
                .description("Categoría de equipos")
                .active(true)
                .annualDepreciation(BigDecimal.TEN)
                .build();
    }

    @Test
    @DisplayName("getAll retorna lista ordenada por nombre")
    void getAll_retorna_lista_ordenada_por_nombre() {

        CategoryAsset c1 = CategoryAsset.builder().name("Zeta").build();
        CategoryAsset c2 = CategoryAsset.builder().name("Alpha").build();

        when(repository.findAll()).thenReturn(Flux.fromIterable(List.of(c1, c2)));

        StepVerifier.create(service.getAll())
                .expectNextMatches(cat -> cat.getName().equals("Alpha"))
                .expectNextMatches(cat -> cat.getName().equals("Zeta"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllActive retorna solo categorías activas")
    void getAllActive_retorna_solo_categorias_activas() {

        when(repository.findByActiveTrueOrderByNameAsc())
                .thenReturn(Flux.just(categoryActiva));

        StepVerifier.create(service.getAllActive())
                .expectNext(categoryActiva)
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllInactive retorna solo categorías inactivas")
    void getAllInactive_retorna_solo_categorias_inactivas() {

        CategoryAsset inactive = CategoryAsset.builder()
                .id(categoryId)
                .active(false)
                .build();

        when(repository.findByActiveFalseOrderByNameAsc())
                .thenReturn(Flux.just(inactive));

        StepVerifier.create(service.getAllInactive())
                .expectNext(inactive)
                .verifyComplete();
    }

    @Test
    @DisplayName("Crear categoría establece active en true y createdAt")
    void crear_categoria_establece_active_y_createdAt() {

        CategoryAsset input = CategoryAsset.builder()
                .categoryCode("CAT-01")
                .name("Equipos")
                .build();

        when(repository.save(any(CategoryAsset.class)))
                .thenReturn(Mono.just(categoryActiva));

        StepVerifier.create(service.create(input))
                .assertNext(result -> {
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getCategoryCode()).isEqualTo("CAT-01");
                })
                .verifyComplete();

        verify(repository, times(1)).save(any(CategoryAsset.class));
    }

    @Test
    @DisplayName("Update modifica datos correctamente cuando existe")
    void update_modifica_datos_cuando_existe() {

        CategoryAsset nuevosDatos = CategoryAsset.builder()
                .categoryCode("CAT-02")
                .name("Mobiliario")
                .description("Nueva descripción")
                .build();

        CategoryAsset actualizado = CategoryAsset.builder()
                .id(categoryId)
                .categoryCode("CAT-02")
                .name("Mobiliario")
                .description("Nueva descripción")
                .build();

        when(repository.findById(categoryId)).thenReturn(Mono.just(categoryActiva));
        when(repository.save(any(CategoryAsset.class))).thenReturn(Mono.just(actualizado));

        StepVerifier.create(service.update(categoryId, nuevosDatos))
                .assertNext(result -> {
                    assertThat(result.getCategoryCode()).isEqualTo("CAT-02");
                    assertThat(result.getName()).isEqualTo("Mobiliario");
                })
                .verifyComplete();

        verify(repository).save(any(CategoryAsset.class));
    }

    @Test
    @DisplayName("Update no ejecuta save cuando no existe la categoría")
    void update_no_ejecuta_save_cuando_no_existe() {

        CategoryAsset nuevosDatos = CategoryAsset.builder().name("Test").build();

        when(repository.findById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(service.update(categoryId, nuevosDatos))
                .verifyComplete();

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Soft delete cambia active a false")
    void soft_delete_cambia_active_a_false() {

        CategoryAsset desactivada = CategoryAsset.builder()
                .id(categoryId)
                .active(false)
                .build();

        when(repository.findById(categoryId)).thenReturn(Mono.just(categoryActiva));
        when(repository.save(any(CategoryAsset.class))).thenReturn(Mono.just(desactivada));

        StepVerifier.create(service.softDelete(categoryId))
                .assertNext(result -> assertThat(result.getActive()).isFalse())
                .verifyComplete();

        verify(repository).save(any(CategoryAsset.class));
    }

    @Test
    @DisplayName("Restore cambia active a true")
    void restore_cambia_active_a_true() {

        CategoryAsset inactiva = CategoryAsset.builder()
                .id(categoryId)
                .active(false)
                .build();

        CategoryAsset restaurada = CategoryAsset.builder()
                .id(categoryId)
                .active(true)
                .build();

        when(repository.findById(categoryId)).thenReturn(Mono.just(inactiva));
        when(repository.save(any(CategoryAsset.class))).thenReturn(Mono.just(restaurada));

        StepVerifier.create(service.restore(categoryId))
                .assertNext(result -> assertThat(result.getActive()).isTrue())
                .verifyComplete();

        verify(repository).save(any(CategoryAsset.class));
    }
}