package pe.edu.vallegrande.configurationservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.configurationservice.application.service.DocumentTypeService;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.configurationservice.domain.model.DocumentType;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.DocumentTypeRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - DocumentTypeService")
class DocumentTypeServiceTest {

    @Mock
    private DocumentTypeRepository repository;

    @InjectMocks
    private DocumentTypeService documentTypeService;

    private DocumentType docType;

    @BeforeEach
    void setUp() {
        docType = new DocumentType();
        docType.setId(1);
        docType.setCode("DNI");
        docType.setDescription("Documento Nacional de Identidad");
        docType.setLength(8);
        docType.setActive(true);
    }

    @Test
    @DisplayName("Crear tipo de documento establece active en true")
    void crear_tipo_documento_establece_active_en_true() {
        DocumentType input = new DocumentType();
        input.setCode("DNI");
        input.setDescription("Documento Nacional de Identidad");

        when(repository.save(any(DocumentType.class))).thenReturn(Mono.just(docType));

        StepVerifier.create(documentTypeService.create(input))
                .assertNext(result -> assertThat(result.getActive()).isTrue())
                .verifyComplete();
    }

    @Test
    @DisplayName("getById retorna 404 cuando no existe")
    void getById_retorna_404_cuando_no_existe() {
        when(repository.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(documentTypeService.getById(99))
                .expectErrorMatches(error ->
                        error instanceof ResourceNotFoundException &&
                        error.getMessage().contains("99"))
                .verify();
    }

    @Test
    @DisplayName("getById retorna el tipo de documento cuando existe")
    void getById_retorna_documento_cuando_existe() {
        when(repository.findById(1)).thenReturn(Mono.just(docType));

        StepVerifier.create(documentTypeService.getById(1))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo(1);
                    assertThat(result.getCode()).isEqualTo("DNI");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Soft delete cambia active a false")
    void soft_delete_cambia_active_a_false() {
        DocumentType inactivo = new DocumentType();
        inactivo.setId(1);
        inactivo.setActive(false);

        when(repository.findById(1)).thenReturn(Mono.just(docType));
        when(repository.save(any(DocumentType.class))).thenReturn(Mono.just(inactivo));

        StepVerifier.create(documentTypeService.delete(1))
                .verifyComplete();

        verify(repository, times(1)).save(any(DocumentType.class));
    }

    @Test
    @DisplayName("Restaurar tipo de documento cambia active a true")
    void restaurar_tipo_documento_cambia_active_a_true() {
        DocumentType inactivo = new DocumentType();
        inactivo.setId(1);
        inactivo.setActive(false);

        DocumentType restaurado = new DocumentType();
        restaurado.setId(1);
        restaurado.setActive(true);

        when(repository.findById(1)).thenReturn(Mono.just(inactivo));
        when(repository.save(any(DocumentType.class))).thenReturn(Mono.just(restaurado));

        StepVerifier.create(documentTypeService.restore(1))
                .verifyComplete();

        verify(repository, times(1)).save(any(DocumentType.class));
    }
}
