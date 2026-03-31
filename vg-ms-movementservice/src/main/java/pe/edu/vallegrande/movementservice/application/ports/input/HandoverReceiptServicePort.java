package pe.edu.vallegrande.movementservice.application.ports.input;

import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptRequest;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptResponse;
import pe.edu.vallegrande.movementservice.application.dto.SignatureRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface HandoverReceiptServicePort {
    Mono<HandoverReceiptResponse> createHandoverReceipt(UUID municipalityId, HandoverReceiptRequest request);
    Mono<HandoverReceiptResponse> updateHandoverReceipt(UUID id, UUID municipalityId, HandoverReceiptRequest request);
    Mono<HandoverReceiptResponse> getHandoverReceiptById(UUID id, UUID municipalityId);
    Flux<HandoverReceiptResponse> getAllHandoverReceipts(UUID municipalityId);
    Mono<HandoverReceiptResponse> getHandoverReceiptByMovement(UUID movementId, UUID municipalityId);
    Flux<HandoverReceiptResponse> getHandoverReceiptsByStatus(String status, UUID municipalityId);
    Flux<HandoverReceiptResponse> getHandoverReceiptsByResponsible(UUID responsibleId, UUID municipalityId);
    Mono<HandoverReceiptResponse> signHandoverReceipt(UUID id, UUID municipalityId, SignatureRequest request);
    Mono<Long> countHandoverReceipts(UUID municipalityId);
    Mono<Long> countHandoverReceiptsByStatus(UUID municipalityId, String status);
    Mono<HandoverReceiptResponse> voidHandoverReceipt(UUID id, UUID municipalityId);
}
