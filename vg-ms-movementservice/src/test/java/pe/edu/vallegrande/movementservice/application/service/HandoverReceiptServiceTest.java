package pe.edu.vallegrande.movementservice.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptRequest;
import pe.edu.vallegrande.movementservice.application.dto.SignatureRequest;
import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import pe.edu.vallegrande.movementservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.movementservice.domain.model.HandoverReceipt;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.client.UserServiceClient;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence.HandoverReceiptRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HandoverReceiptService - Pruebas Unitarias")
class HandoverReceiptServiceTest {

    @Mock
    private HandoverReceiptRepository handoverReceiptRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private HandoverReceiptService handoverReceiptService;

    private UUID municipalityId;
    private UUID receiptId;
    private UUID movementId;
    private UUID deliveringId;
    private UUID receivingId;
    private UUID generatedById;

    @BeforeEach
    void setUp() {
        municipalityId = UUID.randomUUID();
        receiptId      = UUID.randomUUID();
        movementId     = UUID.randomUUID();
        deliveringId   = UUID.randomUUID();
        receivingId    = UUID.randomUUID();
        generatedById  = UUID.randomUUID();
    }

    private HandoverReceipt buildReceipt(String status) {
        return HandoverReceipt.builder()
                .id(receiptId)
                .municipalityId(municipalityId)
                .receiptNumber("ACT-2026-0001")
                .movementId(movementId)
                .deliveringResponsibleId(deliveringId)
                .receivingResponsibleId(receivingId)
                .receiptDate(LocalDate.now())
                .receiptStatus(status)
                .digitalSignatures("{}")
                .generatedBy(generatedById)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private HandoverReceiptRequest buildRequest() {
        return HandoverReceiptRequest.builder()
                .movementId(movementId)
                .deliveringResponsibleId(deliveringId)
                .receivingResponsibleId(receivingId)
                .receiptDate(LocalDate.now())
                .generatedBy(generatedById)
                .build();
    }

    private UserResponse emptyUser() {
        return UserResponse.builder().build();
    }

    private void stubUserClientEmpty() {
        when(userServiceClient.getUserById(any(UUID.class))).thenReturn(Mono.just(emptyUser()));
    }

    @Nested
    @DisplayName("createHandoverReceipt")
    class CreateHandoverReceipt {

        @Test
        @DisplayName("Positivo: crea acta de entrega y retorna respuesta con estado GENERATED")
        void createHandoverReceipt_withValidRequest_returnsCreatedReceipt() {
            HandoverReceipt saved = buildReceipt("GENERATED");
            when(handoverReceiptRepository.countByMunicipalityId(municipalityId)).thenReturn(Mono.just(0L));
            when(handoverReceiptRepository.save(any(HandoverReceipt.class))).thenReturn(Mono.just(saved));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.createHandoverReceipt(municipalityId, buildRequest()))
                    .assertNext(r -> {
                        assertThat(r.getId()).isEqualTo(receiptId);
                        assertThat(r.getReceiptStatus()).isEqualTo("GENERATED");
                        assertThat(r.getMunicipalityId()).isEqualTo(municipalityId);
                        assertThat(r.getMovementId()).isEqualTo(movementId);
                    })
                    .verifyComplete();

            verify(handoverReceiptRepository).save(any(HandoverReceipt.class));
        }

        @Test
        @DisplayName("Negativo: propaga error cuando el repositorio falla al guardar")
        void createHandoverReceipt_whenRepositoryFails_propagatesError() {
            when(handoverReceiptRepository.countByMunicipalityId(municipalityId)).thenReturn(Mono.just(0L));
            when(handoverReceiptRepository.save(any(HandoverReceipt.class)))
                    .thenReturn(Mono.error(new RuntimeException("DB connection error")));

            StepVerifier.create(handoverReceiptService.createHandoverReceipt(municipalityId, buildRequest()))
                    .expectErrorMatches(ex -> ex.getMessage().contains("DB connection error"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("getHandoverReceiptById")
    class GetHandoverReceiptById {

        @Test
        @DisplayName("Positivo: retorna acta cuando existe para el municipio")
        void getHandoverReceiptById_whenReceiptExists_returnsResponse() {
            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.getHandoverReceiptById(receiptId, municipalityId))
                    .assertNext(r -> {
                        assertThat(r.getId()).isEqualTo(receiptId);
                        assertThat(r.getReceiptStatus()).isEqualTo("GENERATED");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: lanza ResourceNotFoundException cuando no existe")
        void getHandoverReceiptById_whenReceiptNotFound_throwsResourceNotFoundException() {
            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.empty());

            StepVerifier.create(handoverReceiptService.getHandoverReceiptById(receiptId, municipalityId))
                    .expectErrorMatches(ex ->
                            ex instanceof ResourceNotFoundException &&
                            ex.getMessage().contains(receiptId.toString()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("getAllHandoverReceipts")
    class GetAllHandoverReceipts {

        @Test
        @DisplayName("Positivo: retorna todas las actas del municipio")
        void getAllHandoverReceipts_whenReceiptsExist_returnsFlux() {
            HandoverReceipt r2 = buildReceipt("FULLY_SIGNED");
            r2.setId(UUID.randomUUID());

            when(handoverReceiptRepository.findByMunicipalityId(municipalityId))
                    .thenReturn(Flux.just(buildReceipt("GENERATED"), r2));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.getAllHandoverReceipts(municipalityId))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: retorna Flux vacío cuando no hay actas")
        void getAllHandoverReceipts_whenNoReceipts_returnsEmptyFlux() {
            when(handoverReceiptRepository.findByMunicipalityId(municipalityId)).thenReturn(Flux.empty());

            StepVerifier.create(handoverReceiptService.getAllHandoverReceipts(municipalityId))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getHandoverReceiptByMovement")
    class GetHandoverReceiptByMovement {

        @Test
        @DisplayName("Positivo: retorna acta vinculada al movimiento")
        void getHandoverReceiptByMovement_whenExists_returnsReceipt() {
            when(handoverReceiptRepository.findByMovementIdAndMunicipalityId(movementId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.getHandoverReceiptByMovement(movementId, municipalityId))
                    .assertNext(r -> assertThat(r.getMovementId()).isEqualTo(movementId))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: lanza ResourceNotFoundException cuando no existe acta para el movimiento")
        void getHandoverReceiptByMovement_whenNotFound_throwsResourceNotFoundException() {
            when(handoverReceiptRepository.findByMovementIdAndMunicipalityId(movementId, municipalityId))
                    .thenReturn(Mono.empty());

            StepVerifier.create(handoverReceiptService.getHandoverReceiptByMovement(movementId, municipalityId))
                    .expectErrorMatches(ex ->
                            ex instanceof ResourceNotFoundException &&
                            ex.getMessage().contains(movementId.toString()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("getHandoverReceiptsByStatus")
    class GetHandoverReceiptsByStatus {

        @Test
        @DisplayName("Positivo: retorna actas filtradas por estado FULLY_SIGNED")
        void getHandoverReceiptsByStatus_whenStatusMatches_returnsFilteredReceipts() {
            when(handoverReceiptRepository.findByReceiptStatusAndMunicipalityId("FULLY_SIGNED", municipalityId))
                    .thenReturn(Flux.just(buildReceipt("FULLY_SIGNED")));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.getHandoverReceiptsByStatus("FULLY_SIGNED", municipalityId))
                    .assertNext(r -> assertThat(r.getReceiptStatus()).isEqualTo("FULLY_SIGNED"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: retorna vacío cuando no hay actas con ese estado")
        void getHandoverReceiptsByStatus_whenNoMatch_returnsEmptyFlux() {
            when(handoverReceiptRepository.findByReceiptStatusAndMunicipalityId("VOIDED", municipalityId))
                    .thenReturn(Flux.empty());

            StepVerifier.create(handoverReceiptService.getHandoverReceiptsByStatus("VOIDED", municipalityId))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getHandoverReceiptsByResponsible")
    class GetHandoverReceiptsByResponsible {

        @Test
        @DisplayName("Positivo: retorna actas donde el usuario es responsable")
        void getHandoverReceiptsByResponsible_whenReceiptsExist_returnsReceipts() {
            when(handoverReceiptRepository.findByResponsibleIdAndMunicipalityId(deliveringId, municipalityId))
                    .thenReturn(Flux.just(buildReceipt("GENERATED")));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.getHandoverReceiptsByResponsible(deliveringId, municipalityId))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("updateHandoverReceipt")
    class UpdateHandoverReceipt {

        @Test
        @DisplayName("Positivo: actualiza acta existente y retorna respuesta actualizada")
        void updateHandoverReceipt_whenReceiptExists_returnsUpdatedResponse() {
            HandoverReceiptRequest request = buildRequest();
            request.setDeliveryObservations("Observacion actualizada");

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            when(handoverReceiptRepository.save(any(HandoverReceipt.class)))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.updateHandoverReceipt(receiptId, municipalityId, request))
                    .assertNext(r -> assertThat(r.getId()).isEqualTo(receiptId))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: lanza ResourceNotFoundException cuando el acta no existe")
        void updateHandoverReceipt_whenReceiptNotFound_throwsResourceNotFoundException() {
            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.empty());

            StepVerifier.create(handoverReceiptService.updateHandoverReceipt(receiptId, municipalityId, buildRequest()))
                    .expectErrorMatches(ex ->
                            ex instanceof ResourceNotFoundException &&
                            ex.getMessage().contains(receiptId.toString()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("signHandoverReceipt")
    class SignHandoverReceipt {

        @Test
        @DisplayName("Positivo: firma de entrega cambia estado a PARTIALLY_SIGNED")
        void signHandoverReceipt_withDeliverySignature_changesStatusToPartiallySigned() {
            SignatureRequest signRequest = SignatureRequest.builder()
                    .signerId(deliveringId)
                    .signatureType("delivery")
                    .observations("Entregado en buen estado")
                    .build();

            HandoverReceipt afterSign = buildReceipt("PARTIALLY_SIGNED");
            afterSign.setDeliverySignatureDate(LocalDateTime.now());

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            when(handoverReceiptRepository.save(any(HandoverReceipt.class))).thenReturn(Mono.just(afterSign));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.signHandoverReceipt(receiptId, municipalityId, signRequest))
                    .assertNext(r -> {
                        assertThat(r.getReceiptStatus()).isEqualTo("PARTIALLY_SIGNED");
                        assertThat(r.getDeliverySignatureDate()).isNotNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Positivo: ambas firmas resultan en estado FULLY_SIGNED")
        void signHandoverReceipt_withBothSignatures_changesStatusToFullySigned() {
            HandoverReceipt partialReceipt = buildReceipt("PARTIALLY_SIGNED");
            partialReceipt.setDeliverySignatureDate(LocalDateTime.now().minusMinutes(5));

            SignatureRequest signRequest = SignatureRequest.builder()
                    .signerId(receivingId)
                    .signatureType("reception")
                    .build();

            HandoverReceipt fullySigned = buildReceipt("FULLY_SIGNED");
            fullySigned.setDeliverySignatureDate(LocalDateTime.now().minusMinutes(5));
            fullySigned.setReceptionSignatureDate(LocalDateTime.now());

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(partialReceipt));
            when(handoverReceiptRepository.save(any(HandoverReceipt.class))).thenReturn(Mono.just(fullySigned));
            stubUserClientEmpty();

            StepVerifier.create(handoverReceiptService.signHandoverReceipt(receiptId, municipalityId, signRequest))
                    .assertNext(r -> {
                        assertThat(r.getReceiptStatus()).isEqualTo("FULLY_SIGNED");
                        assertThat(r.getDeliverySignatureDate()).isNotNull();
                        assertThat(r.getReceptionSignatureDate()).isNotNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: lanza ResourceNotFoundException cuando el acta no existe")
        void signHandoverReceipt_whenReceiptNotFound_throwsResourceNotFoundException() {
            SignatureRequest signRequest = SignatureRequest.builder()
                    .signerId(deliveringId)
                    .signatureType("delivery")
                    .build();

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.empty());

            StepVerifier.create(handoverReceiptService.signHandoverReceipt(receiptId, municipalityId, signRequest))
                    .expectErrorMatches(ex ->
                            ex instanceof ResourceNotFoundException &&
                            ex.getMessage().contains(receiptId.toString()))
                    .verify();
        }
    }

    @Nested
    @DisplayName("countHandoverReceipts / countHandoverReceiptsByStatus")
    class Count {

        @Test
        @DisplayName("Positivo: retorna conteo total de actas del municipio")
        void countHandoverReceipts_whenReceiptsExist_returnsCorrectCount() {
            when(handoverReceiptRepository.countByMunicipalityId(municipalityId)).thenReturn(Mono.just(3L));

            StepVerifier.create(handoverReceiptService.countHandoverReceipts(municipalityId))
                    .assertNext(count -> assertThat(count).isEqualTo(3L))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: retorna 0 cuando no hay actas")
        void countHandoverReceipts_whenNoReceipts_returnsZero() {
            when(handoverReceiptRepository.countByMunicipalityId(municipalityId)).thenReturn(Mono.just(0L));

            StepVerifier.create(handoverReceiptService.countHandoverReceipts(municipalityId))
                    .assertNext(count -> assertThat(count).isZero())
                    .verifyComplete();
        }

        @Test
        @DisplayName("Positivo: retorna conteo de actas por estado GENERATED")
        void countHandoverReceiptsByStatus_whenStatusMatches_returnsCount() {
            when(handoverReceiptRepository.countByMunicipalityIdAndStatus(municipalityId, "GENERATED"))
                    .thenReturn(Mono.just(2L));

            StepVerifier.create(handoverReceiptService.countHandoverReceiptsByStatus(municipalityId, "GENERATED"))
                    .assertNext(count -> assertThat(count).isEqualTo(2L))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Negativo: retorna 0 cuando no hay actas con ese estado")
        void countHandoverReceiptsByStatus_whenNoMatch_returnsZero() {
            when(handoverReceiptRepository.countByMunicipalityIdAndStatus(municipalityId, "VOIDED"))
                    .thenReturn(Mono.just(0L));

            StepVerifier.create(handoverReceiptService.countHandoverReceiptsByStatus(municipalityId, "VOIDED"))
                    .assertNext(count -> assertThat(count).isZero())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("enrichWithUserNames")
    class EnrichWithUserNames {

        @Test
        @DisplayName("Positivo: muestra nombre completo cuando el usuario tiene firstName y lastName")
        void enrichWithUserNames_whenUserHasFullName_displaysFullName() {
            UserResponse userWithName = UserResponse.builder()
                    .id(deliveringId).username("jperez")
                    .firstName("Juan").lastName("Perez")
                    .build();

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            when(userServiceClient.getUserById(deliveringId)).thenReturn(Mono.just(userWithName));
            when(userServiceClient.getUserById(receivingId)).thenReturn(Mono.just(emptyUser()));
            when(userServiceClient.getUserById(generatedById)).thenReturn(Mono.just(emptyUser()));

            StepVerifier.create(handoverReceiptService.getHandoverReceiptById(receiptId, municipalityId))
                    .assertNext(r -> assertThat(r.getDeliveringResponsibleName()).isEqualTo("Juan Perez"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Positivo: muestra username cuando el usuario no tiene nombre completo")
        void enrichWithUserNames_whenUserHasOnlyUsername_displaysUsername() {
            UserResponse userWithUsername = UserResponse.builder()
                    .id(deliveringId).username("jperez")
                    .build();

            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            when(userServiceClient.getUserById(deliveringId)).thenReturn(Mono.just(userWithUsername));
            when(userServiceClient.getUserById(receivingId)).thenReturn(Mono.just(emptyUser()));
            when(userServiceClient.getUserById(generatedById)).thenReturn(Mono.just(emptyUser()));

            StepVerifier.create(handoverReceiptService.getHandoverReceiptById(receiptId, municipalityId))
                    .assertNext(r -> assertThat(r.getDeliveringResponsibleName()).isEqualTo("jperez"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Positivo: muestra 'No asignado' cuando el usuario no tiene datos")
        void enrichWithUserNames_whenUserHasNoData_displaysNoAsignado() {
            when(handoverReceiptRepository.findByIdAndMunicipalityId(receiptId, municipalityId))
                    .thenReturn(Mono.just(buildReceipt("GENERATED")));
            when(userServiceClient.getUserById(any(UUID.class))).thenReturn(Mono.just(emptyUser()));

            StepVerifier.create(handoverReceiptService.getHandoverReceiptById(receiptId, municipalityId))
                    .assertNext(r -> assertThat(r.getDeliveringResponsibleName()).isEqualTo("No asignado"))
                    .verifyComplete();
        }
    }
}
