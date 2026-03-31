package pe.edu.vallegrande.movementservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementResponse;
import pe.edu.vallegrande.movementservice.domain.model.AssetMovement;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence.AssetMovementRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AssetMovementServiceTest {

    @Mock
    private AssetMovementRepository assetMovementRepository;

    private MovementNumberResolver resolver;

    private UUID municipalityId;
    private UUID assetId;
    private UUID requestingUser;

    @BeforeEach
    void setUp() {
        resolver = new MovementNumberResolver(assetMovementRepository);
        municipalityId = UUID.randomUUID();
        assetId = UUID.randomUUID();
        requestingUser = UUID.randomUUID();
    }

    // =========================================================================
    // Escenario 1 — Positivo
    // =========================================================================

    @Test
    @DisplayName("Escenario 1 - Positivo: Sin movimientos previos, genera MV-00001 automáticamente")
    void deberiaGenerarMV00001CuandoNoExistenMovimientosPrevios() {
        // Arrange: repositorio vacío → defaultIfEmpty → "MV-00001"
        when(assetMovementRepository.findLastMovementNumberByMunicipalityId(municipalityId))
                .thenReturn(Mono.empty());

        AssetMovementRequest request = AssetMovementRequest.builder()
                .municipalityId(municipalityId)
                .assetId(assetId)
                .movementType("INITIAL_ASSIGNMENT")
                .reason("Asignación inicial del activo al área de logística")
                .requestingUser(requestingUser)
                .build();

        // Act
        Mono<AssetMovementResponse> resultado = resolver.resolveAndBuildResponse(request);

        // Assert
        StepVerifier.create(resultado)
                .assertNext(response -> {
                    assertThat(response.getMovementNumber())
                            .as("El número de movimiento debe ser MV-00001")
                            .isEqualTo("MV-00001");
                    assertThat(response.getMovementStatus())
                            .as("El estado inicial debe ser REQUESTED")
                            .isEqualTo("REQUESTED");
                    assertThat(response.getActive())
                            .as("El movimiento debe estar activo")
                            .isTrue();
                    assertThat(response.getMunicipalityId())
                            .as("El municipio debe coincidir con el del request")
                            .isEqualTo(municipalityId);
                })
                .verifyComplete();
    }

    // =========================================================================
    // Escenario 2 — Negativo / Excepción
    // =========================================================================

    @Test
    @DisplayName("Escenario 2 - Negativo: movementNumber duplicado lanza IllegalStateException con mensaje 'already exists'")
    void deberiaLanzarIllegalStateExceptionCuandoElNumeroDeMovimientoYaExiste() {
        // Arrange: el repositorio encuentra un movimiento existente con MV-00005
        AssetMovement movimientoExistente = AssetMovement.builder()
                .id(UUID.randomUUID())
                .municipalityId(municipalityId)
                .movementNumber("MV-00005")
                .assetId(UUID.randomUUID())
                .movementType("INITIAL_ASSIGNMENT")
                .movementStatus("COMPLETED")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(assetMovementRepository.findByMovementNumberAndMunicipalityId("MV-00005", municipalityId))
                .thenReturn(Mono.just(movimientoExistente));

        AssetMovementRequest request = AssetMovementRequest.builder()
                .municipalityId(municipalityId)
                .movementNumber("MV-00005")
                .assetId(assetId)
                .movementType("REASSIGNMENT")
                .reason("Reasignación de activo")
                .requestingUser(requestingUser)
                .build();

        // Act
        Mono<AssetMovementResponse> resultado = resolver.resolveAndBuildResponse(request);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error)
                            .as("Debe lanzar IllegalStateException")
                            .isInstanceOf(IllegalStateException.class);
                    assertThat(error.getMessage())
                            .as("El mensaje debe contener 'already exists'")
                            .contains("already exists");
                })
                .verify();
    }



    static class MovementNumberResolver {

        private final AssetMovementRepository repository;

        MovementNumberResolver(AssetMovementRepository repository) {
            this.repository = repository;
        }

        Mono<AssetMovementResponse> resolveAndBuildResponse(AssetMovementRequest request) {
            Mono<String> movementNumberMono;

            if (request.getMovementNumber() == null || request.getMovementNumber().trim().isEmpty()) {
                
                movementNumberMono = repository
                        .findLastMovementNumberByMunicipalityId(request.getMunicipalityId())
                        .map(lastNumber -> {
                            try {
                                int last = Integer.parseInt(lastNumber.substring(3)); // quita "MV-"
                                return String.format("MV-%05d", last + 1);
                            } catch (Exception e) {
                                return "MV-00001";
                            }
                        })
                        .defaultIfEmpty("MV-00001");
            } else {

                movementNumberMono = repository
                        .findByMovementNumberAndMunicipalityId(
                                request.getMovementNumber(), request.getMunicipalityId())
                        .flatMap(existing -> Mono.<String>error(new IllegalStateException(
                                "Movement number " + request.getMovementNumber()
                                        + " already exists for this municipality")))
                        .switchIfEmpty(Mono.just(request.getMovementNumber()));
            }
            return movementNumberMono.map(number -> AssetMovementResponse.builder()
                    .id(UUID.randomUUID())
                    .municipalityId(request.getMunicipalityId())
                    .movementNumber(number)
                    .assetId(request.getAssetId())
                    .movementType(request.getMovementType())
                    .movementStatus("REQUESTED")
                    .reason(request.getReason())
                    .requestingUser(request.getRequestingUser())
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}
