package pe.edu.vallegrande.patrimonio_service.application.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.patrimonio_service.application.ports.input.AssetDisposalUseCase;
import pe.edu.vallegrande.patrimonio_service.application.ports.output.AssetDisposalPersistencePort;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetDisposalResponse;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssetMovementRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.AssignCommitteeRequest;
import pe.edu.vallegrande.patrimonio_service.application.dto.ResolveDisposalRequest;
import pe.edu.vallegrande.patrimonio_service.domain.exception.AssetDisposalNotFoundException;
import pe.edu.vallegrande.patrimonio_service.domain.exception.InvalidDisposalStateException;
import pe.edu.vallegrande.patrimonio_service.domain.model.Asset;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposal;
import pe.edu.vallegrande.patrimonio_service.domain.model.AssetDisposalDetail;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetDisposalDetailRepository;
import pe.edu.vallegrande.patrimonio_service.infrastructure.adapters.output.persistence.repository.AssetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.UUID;

@Service
public class AssetDisposalService implements AssetDisposalUseCase {

    private final AssetDisposalPersistencePort persistencePort;
    private final AssetDisposalDetailRepository disposalDetailRepository;
    private final AssetRepository assetRepository;

    public AssetDisposalService(AssetDisposalPersistencePort persistencePort,
            AssetDisposalDetailRepository disposalDetailRepository,
            AssetRepository assetRepository) {
        this.persistencePort = persistencePort;
        this.disposalDetailRepository = disposalDetailRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public Mono<AssetDisposalResponse> create(AssetDisposalRequest request) {
        AssetDisposal disposal = new AssetDisposal();
        BeanUtils.copyProperties(request, disposal);

        return generateUniqueFileNumber()
                .flatMap(fileNumber -> {
                    disposal.setFileNumber(fileNumber);
                    disposal.setRequestDate(LocalDate.now());
                    disposal.setFileStatus("INITIATED");
                    disposal.setCreatedAt(LocalDateTime.now());

                    return persistencePort.save(disposal);
                })
                .map(this::convertToResponse)
                .onErrorResume(org.springframework.dao.DuplicateKeyException.class, error -> {
                    return create(request);
                });
    }

    @Override
    public Mono<AssetDisposalResponse> getById(UUID id) {
        return persistencePort.findById(id)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)));
    }

    @Override
    public Flux<AssetDisposalResponse> getAll() {
        return persistencePort.findAll()
                .map(this::convertToResponse);
    }

    @Override
    public Flux<AssetDisposalResponse> getByStatus(String fileStatus) {
        return persistencePort.findByFileStatus(fileStatus)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalResponse> getByFileNumber(String fileNumber) {
        return persistencePort.findByFileNumber(fileNumber)
                .map(this::convertToResponse)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException(
                                "Asset disposal not found with file number: " + fileNumber)));
    }

    @Override
    public Flux<AssetDisposalResponse> getByRequestedBy(UUID requestedBy) {
        return persistencePort.findByRequestedBy(requestedBy)
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalResponse> assignCommittee(UUID id, AssignCommitteeRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> {
                    if (!"INITIATED".equals(disposal.getFileStatus())) {
                        return Mono.error(new InvalidDisposalStateException(
                                "Can only update evaluation when status is INITIATED"));
                    }

                    disposal.setFileStatus("UNDER_EVALUATION");
                    disposal.setTechnicalEvaluationDate(LocalDate.now());
                    disposal.setTechnicalReportAuthorId(request.getAssignedBy());
                    disposal.setUpdatedAt(LocalDateTime.now());

                    return persistencePort.save(disposal);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalResponse> resolve(UUID id, ResolveDisposalRequest request) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> {
                    if (!"UNDER_EVALUATION".equals(disposal.getFileStatus())) {
                        return Mono.error(new InvalidDisposalStateException(
                                "Disposal can only be resolved when status is UNDER_EVALUATION"));
                    }

                    String newStatus = request.getApproved() ? "APPROVED" : "REJECTED";
                    disposal.setFileStatus(newStatus);
                    disposal.setApprovedById(request.getApprovedById());
                    disposal.setApprovalDate(LocalDate.now());
                    disposal.setResolutionDate(LocalDate.now());
                    disposal.setResolutionNumber(request.getResolutionNumber());
                    disposal.setObservations(request.getObservations());
                    disposal.setUpdatedAt(LocalDateTime.now());

                    if (request.getApproved()) {
                        return updateAssetsStatusToDisposed(id)
                                .then(persistencePort.save(disposal));
                    } else {
                        return persistencePort.save(disposal);
                    }
                })
                .map(this::convertToResponse);
    }

    private Mono<Void> updateAssetsStatusToDisposed(UUID disposalId) {
        return disposalDetailRepository.findByDisposalId(disposalId)
                .flatMap(detail -> assetRepository.updateAssetStatus(
                        detail.getAssetId(),
                        "BAJA",
                        LocalDateTime.now()))
                .then();
    }

    private Mono<Void> updateAssetsStatusToAvailable(UUID disposalId) {
        return disposalDetailRepository.findByDisposalId(disposalId)
                .flatMap(detail -> assetRepository.updateAssetStatus(
                        detail.getAssetId(),
                        "AVAILABLE",
                        LocalDateTime.now()))
                .then();
    }

    @Override
    public Mono<AssetDisposalResponse> cancel(UUID id, UUID cancelledBy) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> {
                    if ("EXECUTED".equals(disposal.getFileStatus())) {
                        return Mono.error(new InvalidDisposalStateException(
                                "Cannot cancel an already executed disposal"));
                    }

                    disposal.setFileStatus("CANCELLED");
                    disposal.setUpdatedBy(cancelledBy);
                    disposal.setUpdatedAt(LocalDateTime.now());

                    return persistencePort.save(disposal);
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<AssetDisposalResponse> finalize(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> {
                    if (!"APPROVED".equals(disposal.getFileStatus())) {
                        return Mono.error(new InvalidDisposalStateException(
                                "Disposal must be APPROVED before finalizing"));
                    }

                    disposal.setFileStatus("EXECUTED");
                    disposal.setPhysicalRemovalDate(LocalDate.now());
                    disposal.setUpdatedAt(LocalDateTime.now());

                    return persistencePort.save(disposal)
                            .flatMap(saved -> updateAssetsStatusToDisposed(id)
                                    .then(registerDisposalMovements(saved))
                                    .onErrorResume(e -> {
                                        // El MS de movimientos falló, la baja ya fue ejecutada.
                                        // Se registra el error pero no se revierte la baja.
                                        System.err.println("[finalize] Error al registrar movimientos: " + e.getMessage());
                                        return Mono.empty();
                                    })
                                    .thenReturn(saved));
                })
                .map(this::convertToResponse);
    }

    /**
     * Registra un movimiento de tipo ASSET_DISPOSAL en el microservicio de movimientos
     * por cada bien incluido en el expediente de baja, cumpliendo con la trazabilidad
     * requerida por la SBN (SINABIP).
     */
    private Mono<Void> registerDisposalMovements(AssetDisposal disposal) {
        // Movements client removed: skip calling the external movements service.
        // Previously this registered movements via `movementsPort.createMovement(...)`.
        // Now we log and skip to avoid external calls from backend.
        return disposalDetailRepository.findByDisposalId(disposal.getId())
            .concatMap(detail -> assetRepository.findById(detail.getAssetId())
                .flatMap(asset -> {
                    // no-op: external call removed
                    System.err.println("[registerDisposalMovements] skipped movement registration for asset " + asset.getId());
                    return Mono.empty();
                }))
            .then();
    }

    private AssetMovementRequest buildDisposalMovementRequest(AssetDisposal disposal,
            AssetDisposalDetail detail, Asset asset) {
        AssetMovementRequest request = new AssetMovementRequest();
        request.setMunicipalityId(disposal.getMunicipalityId());
        request.setMovementNumber("MV-BAJA-" + detail.getAssetId().toString().substring(0, 8).toUpperCase());
        request.setAssetId(detail.getAssetId());
        request.setMovementType("TEMPORARY_DISPOSAL");
        request.setMovementSubtype(detail.getRecommendation());
        request.setOriginResponsibleId(asset.getCurrentResponsibleId());
        request.setDestinationResponsibleId(detail.getRemovalResponsibleId());
        request.setOriginLocationId(asset.getCurrentLocationId());
        request.setDestinationLocationId(null);
        request.setOriginAreaId(null);
        request.setDestinationAreaId(null);
        request.setRequestDate(disposal.getRequestDate() != null
            ? disposal.getRequestDate().atStartOfDay()
            : null);
        request.setApprovalDate(disposal.getApprovalDate() != null
            ? disposal.getApprovalDate().atStartOfDay()
            : null);
        request.setExecutionDate(disposal.getPhysicalRemovalDate() != null
            ? disposal.getPhysicalRemovalDate().atStartOfDay()
            : null);
        request.setMovementStatus("COMPLETED");
        request.setRequiresApproval(false);
        request.setApprovedBy(disposal.getApprovedById());
        request.setReason(disposal.getReasonDescription());
        request.setObservations(detail.getObservations());
        request.setSpecialConditions(null);
        request.setRequestingUser(disposal.getRequestedBy());
        return request;
    }

    @Override
    public Mono<AssetDisposalResponse> restore(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> {
                    if (!"EXECUTED".equals(disposal.getFileStatus())) {
                        return Mono.error(new InvalidDisposalStateException(
                                "Only EXECUTED disposals can be restored"));
                    }

                    disposal.setFileStatus("RESTORED");
                    disposal.setUpdatedAt(LocalDateTime.now());

                    return updateAssetsStatusToAvailable(id)
                            .then(persistencePort.save(disposal));
                })
                .map(this::convertToResponse);
    }

    @Override
    public Mono<Void> delete(UUID id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(
                        new AssetDisposalNotFoundException("Asset disposal not found with ID: " + id)))
                .flatMap(disposal -> persistencePort.deleteById(id));
    }

    private Mono<String> generateFileNumber() {
        int year = Year.now().getValue();
        return Mono.just(String.format("BAJA-%d-%04d", year, System.currentTimeMillis() % 10000));
    }

    private Mono<String> generateUniqueFileNumber() {
        return generateFileNumber()
                .flatMap(fileNumber -> persistencePort.existsByFileNumber(fileNumber)
                        .flatMap(exists -> {
                            if (exists) {
                                return generateUniqueFileNumber();
                            }
                            return Mono.just(fileNumber);
                        }));
    }

    private AssetDisposalResponse convertToResponse(AssetDisposal disposal) {
        AssetDisposalResponse response = new AssetDisposalResponse();
        BeanUtils.copyProperties(disposal, response);
        return response;
    }
}
