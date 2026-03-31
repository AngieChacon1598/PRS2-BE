package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.SupplierService;
import pe.edu.vallegrande.configurationservice.domain.exception.DuplicateDocumentException;
import pe.edu.vallegrande.configurationservice.domain.model.Supplier;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.SupplierRepository;
import pe.edu.vallegrande.configurationservice.infrastructure.config.JwtContextHelper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - SupplierService")
class SupplierServiceTest {

    @Mock
    private SupplierRepository repository;

    @Mock
    private JwtContextHelper jwtContextHelper;

    @InjectMocks
    private SupplierService supplierService;

    private static final UUID MUNICIPALITY_ID = UUID.fromString("24ad12a5-d9e5-4cdd-91f1-8fd0355c9473");
    private Supplier supplierInput;
    private Supplier supplierGuardado;

    @BeforeEach
    void setUp() {
        supplierInput = Supplier.builder()
                .documentTypesId(1)
                .numeroDocumento("20123456789")
                .legalName("Empresa Ejemplo S.A.C.")
                .tradeName("Ejemplo")
                .address("Av. Principal 123, Lima")
                .phone("987654321")
                .email("contacto@ejemplo.com")
                .companyType("S.A.C.")
                .isStateProvider(false)
                .classification("PEQUEÑA EMPRESA")
                .build();

        supplierGuardado = Supplier.builder()
                .id(UUID.randomUUID())
                .documentTypesId(1)
                .numeroDocumento("20123456789")
                .legalName("Empresa Ejemplo S.A.C.")
                .active(true)
                .municipalityId(MUNICIPALITY_ID)
                .build();
    }

    @Test
    @DisplayName("Crear proveedor exitosamente cuando no existe duplicado")
    void crear_proveedor_exitosamente_cuando_no_existe_duplicado() {
        when(jwtContextHelper.getMunicipalityId()).thenReturn(Mono.just(MUNICIPALITY_ID));
        when(repository.findByDocumentTypesIdAndNumeroDocumento(1, "20123456789"))
                .thenReturn(Mono.empty());
        when(repository.save(any(Supplier.class))).thenReturn(Mono.just(supplierGuardado));

        StepVerifier.create(supplierService.create(supplierInput))
                .assertNext(result -> {
                    assertThat(result.getId()).isNotNull();
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
                    assertThat(result.getNumeroDocumento()).isEqualTo("20123456789");
                })
                .verifyComplete();

        verify(repository, times(1)).save(any(Supplier.class));
    }

    @Test
    @DisplayName("municipalityId se toma del JWT al crear proveedor")
    void crear_proveedor_toma_municipality_id_del_jwt() {
        when(jwtContextHelper.getMunicipalityId()).thenReturn(Mono.just(MUNICIPALITY_ID));
        when(repository.findByDocumentTypesIdAndNumeroDocumento(1, "20123456789"))
                .thenReturn(Mono.empty());
        when(repository.save(any(Supplier.class)))
                .thenAnswer(inv -> Mono.just((Supplier) inv.getArgument(0)));

        StepVerifier.create(supplierService.create(supplierInput))
                .assertNext(result -> {
                    assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
                    assertThat(result.getActive()).isTrue();
                    assertThat(result.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Lanza DuplicateDocumentException cuando ya existe proveedor con mismo documento")
    void crear_proveedor_lanza_excepcion_cuando_documento_duplicado() {
        Supplier existente = Supplier.builder()
                .id(UUID.randomUUID()).documentTypesId(1)
                .numeroDocumento("20123456789").active(true).build();

        when(jwtContextHelper.getMunicipalityId()).thenReturn(Mono.just(MUNICIPALITY_ID));
        when(repository.findByDocumentTypesIdAndNumeroDocumento(1, "20123456789"))
                .thenReturn(Mono.just(existente));

        StepVerifier.create(supplierService.create(supplierInput))
                .expectErrorMatches(error ->
                        error instanceof DuplicateDocumentException &&
                        error.getMessage().contains("20123456789"))
                .verify();

        verify(repository, never()).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Soft delete cambia active a false")
    void soft_delete_cambia_active_a_false() {
        Supplier desactivado = Supplier.builder().id(supplierGuardado.getId()).active(false).build();

        when(repository.findById(supplierGuardado.getId())).thenReturn(Mono.just(supplierGuardado));
        when(repository.save(any(Supplier.class))).thenReturn(Mono.just(desactivado));

        StepVerifier.create(supplierService.softDelete(supplierGuardado.getId()))
                .verifyComplete();

        verify(repository, times(1)).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Restaurar proveedor cambia active a true")
    void restaurar_proveedor_cambia_active_a_true() {
        Supplier inactivo = Supplier.builder().id(UUID.randomUUID()).active(false).build();
        Supplier restaurado = Supplier.builder().id(inactivo.getId()).active(true).build();

        when(repository.findById(inactivo.getId())).thenReturn(Mono.just(inactivo));
        when(repository.save(any(Supplier.class))).thenReturn(Mono.just(restaurado));

        StepVerifier.create(supplierService.restore(inactivo.getId()))
                .verifyComplete();

        verify(repository, times(1)).save(any(Supplier.class));
    }
}
