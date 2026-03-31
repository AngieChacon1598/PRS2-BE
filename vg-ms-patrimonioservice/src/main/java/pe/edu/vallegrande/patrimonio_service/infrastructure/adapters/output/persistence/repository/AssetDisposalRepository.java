package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AssetDisposalRepository extends ReactiveCrudRepository<AssetDisposal, UUID> {

        Mono<AssetDisposal> findByFileNumber(String fileNumber);

        Flux<AssetDisposal> findByFileStatus(String fileStatus);

        Flux<AssetDisposal> findByRequestedBy(UUID requestedBy);

        Flux<AssetDisposal> findByDisposalType(String disposalType);

        Flux<AssetDisposal> findByMunicipalityId(UUID municipalityId);

        Mono<Boolean> existsByFileNumber(String fileNumber);

        @Query("SELECT nextval('file_number_seq')")
        Mono<Long> getNextSequence();

        // Custom queries to avoid R2DBC table prefix issues
        @Query("UPDATE asset_disposals SET file_status = :status, technical_evaluation_date = :evaluationDate, updated_at = :updatedAt WHERE id = :id")
        Mono<Integer> updateStatusAndEvaluationDate(UUID id, String status, LocalDate evaluationDate,
                        java.time.LocalDateTime updatedAt);

        @Query("UPDATE asset_disposals SET file_status = :status, updated_at = :updatedAt WHERE id = :id")
        Mono<Integer> updateStatus(UUID id, String status, java.time.LocalDateTime updatedAt);

        @Query("UPDATE asset_disposals SET file_status = :status, physical_removal_date = :removalDate, updated_at = :updatedAt WHERE id = :id")
        Mono<Integer> updateToExecuted(UUID id, String status, LocalDate removalDate,
                        java.time.LocalDateTime updatedAt);

        @Query("UPDATE asset_disposals SET file_status = :status, approved_by_id = :approvedById, approval_date = :approvalDate, resolution_date = :resolutionDate, resolution_number = :resolutionNumber, observations = :observations, updated_at = :updatedAt WHERE id = :id")
        Mono<Integer> updateResolution(UUID id, String status, UUID approvedById, LocalDate approvalDate,
                        LocalDate resolutionDate, String resolutionNumber, String observations,
                        LocalDateTime updatedAt);
}
