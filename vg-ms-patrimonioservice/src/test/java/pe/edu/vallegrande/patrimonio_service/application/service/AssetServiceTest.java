package pe.edu.vallegrande.patrimonio_service.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.vallegrande.patrimonio_service.application.dto.AssetRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.CambioEstadoRequest;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetPersistencePort persistencePort;

    @InjectMocks
    private AssetService assetService;

    private UUID assetId;
    private Asset asset;
    private AssetRequest assetRequest;

    @BeforeEach
    void setUp() {
        assetId = UUID.randomUUID();

        asset = new Asset();
        asset.setId(assetId);
        asset.setAssetCode("AST-001");
        asset.setDescription("Laptop Dell Latitude");
        asset.setAcquisitionValue(new BigDecimal("3500.00"));
        asset.setCurrentValue(new BigDecimal("3500.00"));
        asset.setAssetStatus("AC");

        assetRequest = new AssetRequest();
        assetRequest.setAssetCode("AST-001");
        assetRequest.setDescription("Laptop Dell Latitude");
        assetRequest.setAcquisitionValue(new BigDecimal("3500.00"));
    }

    @Test
    void create_WhenValidRequest_ShouldReturnSavedAsset() {
        // Arrange
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.create(assetRequest);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(asset.getAssetCode(), response.getAssetCode());
                assertEquals(asset.getAcquisitionValue(), response.getAcquisitionValue());
            })
            .verifyComplete();
        
        verify(persistencePort, times(1)).save(any(Asset.class));
    }

    @Test
    void getById_WhenAssetExists_ShouldReturnAsset() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.getById(assetId);

        // Assert
        StepVerifier.create(result)
            .expectNextMatches(response -> response.getId().equals(assetId))
            .verifyComplete();
    }

    @Test
    void getById_WhenAssetDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        when(persistencePort.findById(assetId)).thenReturn(Mono.empty());

        // Act
        Mono<AssetResponse> result = assetService.getById(assetId);

        // Assert
        StepVerifier.create(result)
            .expectError(AssetNotFoundException.class)
            .verify();
    }

    @Test
    void changeStatus_WhenAssetExists_ShouldUpdateStatus() {
        // Arrange
        String nuevoEstado = "BA";
        CambioEstadoRequest cambioRequest = new CambioEstadoRequest();
        cambioRequest.setNuevoEstado(nuevoEstado);
        cambioRequest.setObservaciones("Baja por obsolescencia");

        Asset assetActualizado = new Asset();
        assetActualizado.setId(assetId);
        assetActualizado.setAssetStatus(nuevoEstado);

        when(persistencePort.findById(assetId)).thenReturn(Mono.just(asset));
        when(persistencePort.save(any(Asset.class))).thenReturn(Mono.just(assetActualizado));

        // Act
        Mono<AssetResponse> result = assetService.changeStatus(assetId, cambioRequest);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getAssetStatus().equals(nuevoEstado))
                .verifyComplete();

        verify(persistencePort).save(argThat(a -> a.getAssetStatus().equals(nuevoEstado)));
    }

    @Test
    void findByAssetCode_WhenExists_ShouldReturnAsset() {
        // Arrange
        String code = "AST-001";
        when(persistencePort.findByAssetCode(code)).thenReturn(Mono.just(asset));

        // Act
        Mono<AssetResponse> result = assetService.findByAssetCode(code);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(response -> response.getAssetCode().equals(code))
                .verifyComplete();
    }
}
