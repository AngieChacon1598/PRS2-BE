package pe.edu.vallegrande.movementservice.application.ports.output;

import pe.edu.vallegrande.movementservice.domain.model.HandoverReceipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface HandoverReceiptRepositoryPort {
    Mono<HandoverReceipt> save(HandoverReceipt handoverReceipt);
    Mono<HandoverReceipt> findById(UUID id);
    Mono<HandoverReceipt> findByIdAndMunicipalityId(UUID id, UUID municipalityId);
    Flux<HandoverReceipt> findByMunicipalityId(UUID municipalityId);
    Mono<HandoverReceipt> findByMovementIdAndMunicipalityId(UUID movementId, UUID municipalityId);
    Flux<HandoverReceipt> findByReceiptStatusAndMunicipalityId(String receiptStatus, UUID municipalityId);
    Flux<HandoverReceipt> findByResponsibleIdAndMunicipalityId(UUID responsibleId, UUID municipalityId);
    Mono<Long> countByMunicipalityId(UUID municipalityId);
    Mono<Long> countByMunicipalityIdAndStatus(UUID municipalityId, String status);
}
