package pe.edu.vallegrande.movementservice.infrastructure.adapters.output.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.movementservice.domain.model.HandoverReceipt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface HandoverReceiptRepository extends R2dbcRepository<HandoverReceipt, UUID> {

    Flux<HandoverReceipt> findByMunicipalityId(UUID municipalityId);

    Mono<HandoverReceipt> findByIdAndMunicipalityId(UUID id, UUID municipalityId);

    Mono<HandoverReceipt> findByMovementIdAndMunicipalityId(UUID movementId, UUID municipalityId);

    Flux<HandoverReceipt> findByReceiptStatusAndMunicipalityId(String receiptStatus, UUID municipalityId);

    @Query("SELECT * FROM handover_receipts WHERE municipality_id = :municipalityId " +
           "AND (delivering_responsible_id = :responsibleId OR receiving_responsible_id = :responsibleId)")
    Flux<HandoverReceipt> findByResponsibleIdAndMunicipalityId(UUID responsibleId, UUID municipalityId);

    @Query("SELECT COUNT(*) FROM handover_receipts WHERE municipality_id = :municipalityId")
    Mono<Long> countByMunicipalityId(UUID municipalityId);

    @Query("SELECT COUNT(*) FROM handover_receipts WHERE municipality_id = :municipalityId AND receipt_status = :status")
    Mono<Long> countByMunicipalityIdAndStatus(UUID municipalityId, String status);
}
