package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalDetailUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalDetailPersistencePort;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetDisposalDetailNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.application.dto.*;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AssetDisposalDetailService implements AssetDisposalDetailUseCase {

    private final AssetDisposalDetailPersistencePort persistencePort;
    private final AssetDisposalDetailRepository repository; // Para métodos custom

    public AssetDisposalDetailService(AssetDisposalDetailPersistencePort persistencePort,
            AssetDisposalDetailRepository repository) {
        this.persistencePort = persistencePort;
        this.repository = repository;
    }

    @Override
    public Mono<AssetDisposalDetailResponse> create(AssetDisposalDetailRequest request) {
        return repository.existsByAssetIdInActiveDisposal(request.getAssetId())
                .flatMap(existsInActive -> {
                    if (Boolean.TRUE.equals(existsInActive)) {
                        return Mono.error(new IllegalStateException(
                                "El bien ya se encuentra asignado en otro expediente de baja activo. " +
                                        "No se puede agregar el mismo bien a múltiples expedientes."));
                    }
                    AssetDisposalDetail detail = new AssetDisposalDetail();
                    BeanUtils.copyProperties(request, detail);
                    detail.setCreatedAt(LocalDateTime.now());
                    detail.setLastModifiedAt(LocalDateTime.now());
                    return persistencePort.save(detail);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalDetailResponse> getById(UUID id) {
        return persistencePort.findById(id)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalDetailNotFoundException("Asset disposal detail not found with ID: " + id)));
    }

    @Override
    public Flux<AssetDisposalDetailResponse> getByDisposalId(UUID disposalId) {
        return repository.findByDisposalIdWithAssetName(disposalId)
                .map(dto -> {
                    AssetDisposalDetailWithAssetResponse response = new AssetDisposalDetailWithAssetResponse();
                    response.setId(dto.getId());
                    response.setAssetId(dto.getAssetId());
                    response.setAssetCode(dto.getAssetCode());
                    response.setAssetDescription(dto.getDescription());
                    response.setRecommendation(dto.getRecommendation());
                    response.setTechnicalOpinion(dto.getTechnicalOpinion());
                    response.setObservations(dto.getObservations());
                    response.setAssetModel(dto.getModel());
                    return response;
                });
    }

    @Override
    public Flux<AssetDisposalDetailResponse> getByAssetId(UUID assetId) {
        return persistencePort.findByAssetId(assetId)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalDetailResponse> addTechnicalOpinion(UUID id, TechnicalOpinionRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalDetailNotFoundException("Asset disposal detail not found with ID: " + id)))
                .flatMap(detail -> {
                    LocalDateTime now = LocalDateTime.now();
                    return repository.updateTechnicalOpinion(
                            id,
                            request.getTechnicalOpinion(),
                            request.getRecommendation(),
                            request.getObservations(),
                            now
                    ).then(persistencePort.findById(id));
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalDetailResponse> executeRemoval(UUID id, ExecuteRemovalRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalDetailNotFoundException("Asset disposal detail not found with ID: " + id)))
                .flatMap(detail -> {
                    detail.setRemovalDate(LocalDate.now());
                    detail.setRemovalResponsibleId(request.getRemovalResponsibleId());
                    detail.setFinalDestination(request.getFinalDestination());
                    detail.setLastModifiedAt(LocalDateTime.now());

                    return persistencePort.save(detail);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalDetailNotFoundException("Asset disposal detail not found with ID: " + id)))
                .flatMap(detail -> persistencePort.deleteById(id));
    }

    @Override
    public Flux<UUID> findActiveAssetIds() {
        return persistencePort.findActiveAssetIds();
    }

    private AssetDisposalDetailResponse convertToResponse(AssetDisposalDetail detail) {
        AssetDisposalDetailResponse response = new AssetDisposalDetailResponse();
        BeanUtils.copyProperties(detail, response);
        return response;
    }
}
