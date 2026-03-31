package pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalDetailWithAssetName;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AssetDisposalDetailRepository extends ReactiveCrudRepository<AssetDisposalDetail, UUID> {

        Flux<AssetDisposalDetail> findByDisposalId(UUID disposalId);

        Mono<AssetDisposalDetail> findByDisposalIdAndAssetId(UUID disposalId, UUID assetId);

        Flux<AssetDisposalDetail> findByAssetId(UUID assetId);

        Flux<AssetDisposalDetail> findByRecommendation(String recommendation);

        Mono<Long> countByDisposalId(UUID disposalId);

        Mono<Boolean> existsByAssetId(UUID assetId);

        @Query("SELECT EXISTS(SELECT 1 FROM asset_disposal_detail d " +
                        "JOIN asset_disposals ad ON d.disposal_id = ad.id " +
                        "WHERE d.asset_id = :assetId " +
                        "AND ad.file_status IN ('INITIATED', 'UNDER_EVALUATION', 'APPROVED'))")
        Mono<Boolean> existsByAssetIdInActiveDisposal(UUID assetId);

        @Query("SELECT d.id AS id, d.asset_id AS asset_id, a.asset_code AS asset_code, a.description AS description, a.model AS model, d.recommendation AS recommendation, d.technical_opinion AS technical_opinion, d.observations AS observations FROM asset_disposal_detail d JOIN assets a ON d.asset_id = a.id WHERE d.disposal_id = :disposalId")
        Flux<AssetDisposalDetailWithAssetName> findByDisposalIdWithAssetName(UUID disposalId);

        @Query("SELECT DISTINCT d.asset_id FROM asset_disposal_detail d " +
                        "JOIN asset_disposals ad ON d.disposal_id = ad.id " +
                        "WHERE ad.file_status IN ('INITIATED', 'UNDER_EVALUATION', 'APPROVED')")
        Flux<UUID> findActiveAssetIds();

        @Query("UPDATE asset_disposal_detail SET technical_opinion = :technicalOpinion, recommendation = :recommendation, observations = :observations, updated_at = :updatedAt WHERE id = :id")
        Mono<Integer> updateTechnicalOpinion(UUID id, String technicalOpinion, String recommendation,
                        String observations,
                        LocalDateTime updatedAt);
}
