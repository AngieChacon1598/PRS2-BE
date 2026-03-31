package pe.edu.vallegrande.movementservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptRequest;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptResponse;
import pe.edu.vallegrande.movementservice.application.dto.SignatureRequest;
import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import pe.edu.vallegrande.movementservice.application.ports.input.HandoverReceiptServicePort;
import pe.edu.vallegrande.movementservice.domain.exception.ResourceNotFoundException;
import pe.edu.vallegrande.movementservice.domain.model.HandoverReceipt;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.client.UserServiceClient;
import pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence.HandoverReceiptRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandoverReceiptService implements HandoverReceiptServicePort {

    private final HandoverReceiptRepository handoverReceiptRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public Mono<HandoverReceiptResponse> createHandoverReceipt(UUID municipalityId, HandoverReceiptRequest request) {
        log.info("Creating handover receipt for movement {} in municipality {}", request.getMovementId(), municipalityId);

        return generateReceiptNumber(municipalityId)
                .flatMap(receiptNumber -> {
                    HandoverReceipt receipt = HandoverReceipt.builder()
                            .municipalityId(municipalityId)
                            .receiptNumber(receiptNumber)
                            .movementId(request.getMovementId())
                            .deliveringResponsibleId(request.getDeliveringResponsibleId())
                            .receivingResponsibleId(request.getReceivingResponsibleId())
                            .witness1Id(request.getWitness1Id())
                            .witness2Id(request.getWitness2Id())
                            .receiptDate(request.getReceiptDate() != null ? request.getReceiptDate() : LocalDate.now())
                            .receiptStatus("GENERATED")
                            .deliveryObservations(request.getDeliveryObservations())
                            .receptionObservations(request.getReceptionObservations())
                            .specialConditions(request.getSpecialConditions())
                            .digitalSignatures("{}")
                            .generatedBy(request.getGeneratedBy())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return handoverReceiptRepository.save(receipt);
                })
                .flatMap(this::enrichWithUserNames)
                .doOnSuccess(response -> log.info("Handover receipt created with ID: {}", response.getId()))
                .doOnError(error -> log.error("Error creating handover receipt", error));
    }

    @Transactional
    public Mono<HandoverReceiptResponse> updateHandoverReceipt(UUID id, UUID municipalityId, HandoverReceiptRequest request) {
        log.info("Updating handover receipt {} in municipality {}", id, municipalityId);


        return handoverReceiptRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Handover receipt not found with ID: " + id)))
                .flatMap(existingReceipt -> {
                    existingReceipt.setMovementId(request.getMovementId());
                    existingReceipt.setDeliveringResponsibleId(request.getDeliveringResponsibleId());
                    existingReceipt.setReceivingResponsibleId(request.getReceivingResponsibleId());
                    existingReceipt.setWitness1Id(request.getWitness1Id());
                    existingReceipt.setWitness2Id(request.getWitness2Id());
                    existingReceipt.setReceiptDate(request.getReceiptDate() != null ? request.getReceiptDate() : existingReceipt.getReceiptDate());
                    existingReceipt.setDeliveryObservations(request.getDeliveryObservations());
                    existingReceipt.setReceptionObservations(request.getReceptionObservations());
                    existingReceipt.setSpecialConditions(request.getSpecialConditions());
                    existingReceipt.setGeneratedBy(request.getGeneratedBy());
                    existingReceipt.setUpdatedAt(LocalDateTime.now());


                    return handoverReceiptRepository.save(existingReceipt);
                })
                .flatMap(this::enrichWithUserNames)
                .doOnSuccess(response -> log.info("Handover receipt updated with ID: {}", response.getId()))
                .doOnError(error -> log.error("Error updating handover receipt: {}", id, error));
    }

    public Mono<HandoverReceiptResponse> getHandoverReceiptById(UUID id, UUID municipalityId) {
        return handoverReceiptRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Handover receipt not found with ID: " + id)))
                .flatMap(this::enrichWithUserNames);
    }

    public Flux<HandoverReceiptResponse> getAllHandoverReceipts(UUID municipalityId) {
        return handoverReceiptRepository.findByMunicipalityId(municipalityId)
                .flatMap(this::enrichWithUserNames);
    }

    public Mono<HandoverReceiptResponse> getHandoverReceiptByMovement(UUID movementId, UUID municipalityId) {
        return handoverReceiptRepository.findByMovementIdAndMunicipalityId(movementId, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Handover receipt not found for movement: " + movementId)))
                .flatMap(this::enrichWithUserNames);
    }

    public Flux<HandoverReceiptResponse> getHandoverReceiptsByStatus(String status, UUID municipalityId) {
        return handoverReceiptRepository.findByReceiptStatusAndMunicipalityId(status, municipalityId)
                .flatMap(this::enrichWithUserNames);
    }

    public Flux<HandoverReceiptResponse> getHandoverReceiptsByResponsible(UUID responsibleId, UUID municipalityId) {
        return handoverReceiptRepository.findByResponsibleIdAndMunicipalityId(responsibleId, municipalityId)
                .flatMap(this::enrichWithUserNames);
    }

    @Transactional
    public Mono<HandoverReceiptResponse> signHandoverReceipt(UUID id, UUID municipalityId, SignatureRequest request) {
        log.info("Processing signature for handover receipt {} by user {}", id, request.getSignerId());

        return handoverReceiptRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Handover receipt not found with ID: " + id)))
                .flatMap(receipt -> {
                    LocalDateTime now = LocalDateTime.now();

                    if ("delivery".equals(request.getSignatureType())) {
                        receipt.setDeliverySignatureDate(now);
                        if (request.getObservations() != null) {
                            receipt.setDeliveryObservations(request.getObservations());
                        }
                        String deliverySignature = String.format(
                                "{\"delivery\":{\"signerId\":\"%s\",\"signedAt\":\"%s\",\"type\":\"delivery\"}}",
                                request.getSignerId(), now);
                        receipt.setDigitalSignatures(deliverySignature);
                    } else if ("reception".equals(request.getSignatureType())) {
                        receipt.setReceptionSignatureDate(now);
                        if (request.getObservations() != null) {
                            receipt.setReceptionObservations(request.getObservations());
                        }
                        String receptionSignature = String.format(
                                "{\"reception\":{\"signerId\":\"%s\",\"signedAt\":\"%s\",\"type\":\"reception\"}}",
                                request.getSignerId(), now);
                        receipt.setDigitalSignatures(receptionSignature);
                    }

                    receipt.setReceiptStatus(calculateReceiptStatus(receipt));
                    receipt.setUpdatedAt(now);

                    return handoverReceiptRepository.save(receipt);
                })
                .flatMap(this::enrichWithUserNames)
                .doOnSuccess(response -> log.info("Signature processed for handover receipt: {}", response.getId()))
                .doOnError(error -> log.error("Error processing signature for handover receipt: {}", id, error));
    }

    public Mono<Long> countHandoverReceipts(UUID municipalityId) {
        return handoverReceiptRepository.countByMunicipalityId(municipalityId);
    }

    public Mono<Long> countHandoverReceiptsByStatus(UUID municipalityId, String status) {
        return handoverReceiptRepository.countByMunicipalityIdAndStatus(municipalityId, status);
    }

    @Transactional
    public Mono<HandoverReceiptResponse> voidHandoverReceipt(UUID id, UUID municipalityId) {
        log.info("Voiding handover receipt {} in municipality {}", id, municipalityId);

        return handoverReceiptRepository.findByIdAndMunicipalityId(id, municipalityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Handover receipt not found with ID: " + id)))
                .flatMap(receipt -> {
                    if ("VOIDED".equals(receipt.getReceiptStatus())) {
                        return Mono.error(new IllegalStateException("Handover receipt is already voided"));
                    }
                    receipt.setReceiptStatus("VOIDED");
                    receipt.setUpdatedAt(LocalDateTime.now());
                    return handoverReceiptRepository.save(receipt);
                })
                .flatMap(this::enrichWithUserNames)
                .doOnSuccess(r -> log.info("Handover receipt voided: {}", r.getId()))
                .doOnError(e -> log.error("Error voiding handover receipt: {}", id, e));
    }

    private Mono<HandoverReceiptResponse> enrichWithUserNames(HandoverReceipt receipt) {
        return Mono.zip(
                        getUserOrEmpty(receipt.getDeliveringResponsibleId()),
                        getUserOrEmpty(receipt.getReceivingResponsibleId()),
                        getUserOrEmpty(receipt.getWitness1Id()),
                        getUserOrEmpty(receipt.getWitness2Id()),
                        getUserOrEmpty(receipt.getGeneratedBy())
                )
                .map(tuple -> mapToResponseWithNames(receipt,
                        tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4(), tuple.getT5()));
    }

    private Mono<UserResponse> getUserOrEmpty(UUID userId) {
        if (userId == null) {
            return Mono.just(UserResponse.builder().build());
        }
        return userServiceClient.getUserById(userId)
                .defaultIfEmpty(UserResponse.builder().build());
    }

    private Mono<String> generateReceiptNumber(UUID municipalityId) {
        return handoverReceiptRepository.countByMunicipalityId(municipalityId)
                .map(count -> String.format("ACT-%d-%04d", LocalDate.now().getYear(), count + 1));
    }

    private String calculateReceiptStatus(HandoverReceipt receipt) {
        boolean hasDeliverySignature = receipt.getDeliverySignatureDate() != null;
        boolean hasReceptionSignature = receipt.getReceptionSignatureDate() != null;

        if (hasDeliverySignature && hasReceptionSignature) {
            return "FULLY_SIGNED";
        } else if (hasDeliverySignature || hasReceptionSignature) {
            return "PARTIALLY_SIGNED";
        } else {
            return "GENERATED";
        }
    }

    private HandoverReceiptResponse mapToResponseWithNames(HandoverReceipt receipt, UserResponse deliveringUser,
                                                            UserResponse receivingUser, UserResponse witness1,
                                                            UserResponse witness2, UserResponse generatedByUser) {
        return HandoverReceiptResponse.builder()
                .id(receipt.getId())
                .municipalityId(receipt.getMunicipalityId())
                .receiptNumber(receipt.getReceiptNumber())
                .movementId(receipt.getMovementId())
                .deliveringResponsibleId(receipt.getDeliveringResponsibleId())
                .receivingResponsibleId(receipt.getReceivingResponsibleId())
                .witness1Id(receipt.getWitness1Id())
                .witness2Id(receipt.getWitness2Id())
                .receiptDate(receipt.getReceiptDate())
                .deliverySignatureDate(receipt.getDeliverySignatureDate())
                .receptionSignatureDate(receipt.getReceptionSignatureDate())
                .receiptStatus(receipt.getReceiptStatus())
                .deliveryObservations(receipt.getDeliveryObservations())
                .receptionObservations(receipt.getReceptionObservations())
                .specialConditions(receipt.getSpecialConditions())
                .digitalSignatures(receipt.getDigitalSignatures())
                .generatedBy(receipt.getGeneratedBy())
                .createdAt(receipt.getCreatedAt())
                .updatedAt(receipt.getUpdatedAt())
                .deliveringResponsibleName(getUserDisplayName(deliveringUser))
                .receivingResponsibleName(getUserDisplayName(receivingUser))
                .witness1Name(getUserDisplayName(witness1))
                .witness2Name(getUserDisplayName(witness2))
                .generatedByName(getUserDisplayName(generatedByUser))
                .build();
    }

    private String getUserDisplayName(UserResponse user) {
        if (user == null || user.getUsername() == null) {
            return "No asignado";
        }
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return user.getUsername();
    }
}
