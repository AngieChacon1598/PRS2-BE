package pe.edu.vallegrande.ms_inventory.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.ms_inventory.application.dto.AssetDTO;
import pe.edu.vallegrande.ms_inventory.application.dto.InventoryFormDataDTO;
import pe.edu.vallegrande.ms_inventory.application.dto.PhysicalInventoryDTO;
import pe.edu.vallegrande.ms_inventory.application.mapper.PhysicalInventoryMapper;
import pe.edu.vallegrande.ms_inventory.application.ports.in.PhysicalInventoryUseCase;
import pe.edu.vallegrande.ms_inventory.application.ports.out.AssetClientPort;
import pe.edu.vallegrande.ms_inventory.application.ports.out.ConfigurationClientPort;
import pe.edu.vallegrande.ms_inventory.application.ports.out.PhysicalInventoryDetailPersistencePort;
import pe.edu.vallegrande.ms_inventory.application.ports.out.PhysicalInventoryPersistencePort;
import pe.edu.vallegrande.ms_inventory.application.ports.out.UserClientPort;
import pe.edu.vallegrande.ms_inventory.domain.exception.BadRequestException;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventoryDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhysicalInventoryUseCaseImpl implements PhysicalInventoryUseCase {

    private final PhysicalInventoryPersistencePort persistencePort;
    private final PhysicalInventoryDetailPersistencePort detailPersistencePort;
    private final AssetClientPort assetClientPort;
    private final UserClientPort userClientPort;
    private final ConfigurationClientPort configurationClientPort;

    // ---------------------------------------------------------------
    // LISTAR TODOS
    // ---------------------------------------------------------------
    @Override
    public Flux<PhysicalInventory> listAll() {
        return persistencePort.findAll();
    }

    @Override
    public Mono<PhysicalInventory> getById(UUID id) {
        return persistencePort.findById(id);
    }

    // ---------------------------------------------------------------
    // CREAR
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public Mono<PhysicalInventoryDTO> create(PhysicalInventoryDTO dto) {
        return getAuthenticatedUserId()
                .switchIfEmpty(Mono.error(new BadRequestException("No se pudo obtener el usuario autenticado")))
                .flatMap(userId -> {
                    PhysicalInventory inventory = PhysicalInventoryMapper.toDomain(dto);
                    try {
                        validateInventoryRules(inventory);
                    } catch (BadRequestException ex) {
                        return Mono.error(ex);
                    }

                    inventory.setCreatedAt(LocalDateTime.now());
                    inventory.setUpdatedAt(LocalDateTime.now());
                    inventory.setInventoryStatus("PLANNED");
                    inventory.setCreatedBy(userId);

                    if (inventory.getMunicipalityId() == null)
                        inventory.setMunicipalityId(UUID.fromString("24ad12a5-d9e5-4cdd-91f1-8fd0355c9473"));
                    if (inventory.getInventoryTeam() == null)
                        inventory.setInventoryTeam(JsonNodeFactory.instance.arrayNode());
                    if (inventory.getAttachedDocuments() == null)
                        inventory.setAttachedDocuments(JsonNodeFactory.instance.arrayNode());
                    if (inventory.getProgressPercentage() == null)
                        inventory.setProgressPercentage(0.0);

                    return persistencePort.save(inventory)
                            .flatMap(saved -> {
                                UUID municipalityId = saved.getMunicipalityId();
                                UUID areaId = saved.getAreaId();
                                UUID categoryId = saved.getCategoryId();
                                UUID locationId = saved.getLocationId();

                                log.info("=== CREANDO INVENTARIO === Tipo: {}, Municipio: {}", saved.getInventoryType(),
                                        municipalityId);

                                Flux<AssetDTO> assetsToInclude;

                                switch (saved.getInventoryType()) {
                                    case "GENERAL":
                                        assetsToInclude = assetClientPort.getAssets(municipalityId, null, null, null)
                                                .filter(this::isActiveAsset);
                                        break;
                                    case "SELECTIVE":
                                        assetsToInclude = assetClientPort.getAssets(municipalityId, areaId, categoryId, locationId)
                                                .filter(this::isActiveAsset)
                                                .filter(asset -> {
                                                    if (categoryId != null)
                                                        return categoryId.equals(asset.getCategoryId());
                                                    if (areaId != null)
                                                        return areaId.equals(asset.getCurrentAreaId());
                                                    if (locationId != null)
                                                        return locationId.equals(asset.getCurrentLocationId());
                                                    return true;
                                                });
                                        break;
                                    case "SPECIAL":
                                        assetsToInclude = assetClientPort.getAssets(municipalityId, null, null, null)
                                                .filter(this::isActiveAsset);
                                        break;
                                    default:
                                        assetsToInclude = Flux.empty();
                                }

                                return assetsToInclude
                                        .buffer(50)
                                        .concatMap(batch -> Flux.fromIterable(batch)
                                                .flatMap(assetDto -> {
                                                    PhysicalInventoryDetail detail = new PhysicalInventoryDetail();
                                                    detail.setInventoryId(saved.getId());
                                                    detail.setMunicipalityId(saved.getMunicipalityId());
                                                    detail.setAssetId(assetDto.getId());
                                                    detail.setFoundStatus("FOUND");
                                                    detail.setVerifiedBy(saved.getGeneralResponsibleId());
                                                    detail.setCreatedAt(LocalDateTime.now());
                                                    detail.setUpdatedAt(LocalDateTime.now());
                                                    detail.setObservations("");
                                                    detail.setPhotographs(JsonNodeFactory.instance.arrayNode());
                                                    detail.setRequiresAction(false);
                                                    detail.setAdditionalEvidence(JsonNodeFactory.instance.arrayNode());
                                                    return detailPersistencePort.create(detail);
                                                }, 10))
                                        .then(Mono.just(PhysicalInventoryMapper.toDTO(saved)));
                            });
                });
    }

    // -------------------------
    // MÉTODO AUXILIAR: OBTENER USERID DEL TOKEN JWT
    // -------------------------
    private Mono<UUID> getAuthenticatedUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    JwtAuthenticationToken auth = (JwtAuthenticationToken) securityContext.getAuthentication();
                    String userId = auth.getToken().getClaimAsString("sub");
                    return UUID.fromString(userId);
                });
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public Mono<PhysicalInventory> update(UUID id, PhysicalInventory inventory) {

        try {
            validateInventoryRules(inventory);
        } catch (BadRequestException ex) {
            return Mono.error(ex);
        }

        return persistencePort.findById(id)
                .flatMap(existing -> getAuthenticatedUserId()
                .map(userId -> {
                    existing.setInventoryNumber(inventory.getInventoryNumber());
                    existing.setInventoryType(inventory.getInventoryType());
                    existing.setDescription(inventory.getDescription());
                    existing.setAreaId(inventory.getAreaId());
                    existing.setCategoryId(inventory.getCategoryId());
                    existing.setLocationId(inventory.getLocationId());
                    existing.setPlannedStartDate(inventory.getPlannedStartDate());
                    existing.setPlannedEndDate(inventory.getPlannedEndDate());
                    existing.setGeneralResponsibleId(inventory.getGeneralResponsibleId());
                    existing.setIncludesMissing(inventory.getIncludesMissing());
                    existing.setIncludesSurplus(inventory.getIncludesSurplus());
                    existing.setRequiresPhotos(inventory.getRequiresPhotos());
                    existing.setObservations(inventory.getObservations());
                    if (inventory.getInventoryTeam() != null)
                        existing.setInventoryTeam(inventory.getInventoryTeam());
                    if (inventory.getAttachedDocuments() != null)
                        existing.setAttachedDocuments(inventory.getAttachedDocuments());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setUpdatedBy(userId);
                    return existing;
                }))
                .flatMap(persistencePort::save);
    }

    // ---------------------------------------------------------------
    // DELETE LÓGICO
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public Mono<Void> deleteLogical(UUID id) {
        return Mono.zip(persistencePort.findById(id), getAuthenticatedUserId())
                .switchIfEmpty(Mono.error(new RuntimeException("Inventario no encontrado o usuario no autenticado")))
                .flatMap(tuple -> {
                    PhysicalInventory existing = tuple.getT1();
                    UUID userId = tuple.getT2();
                    existing.setInventoryStatus("CANCELLED");
                    existing.setUpdatedBy(userId);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return persistencePort.save(existing).then();
                });
    }

    // ---------------------------------------------------------------
    // LISTAR CON DETALLES
    // ---------------------------------------------------------------
    @Override
    public Flux<PhysicalInventory> listAllWithDetails() {
        return persistencePort.findAll()
                .flatMap(inventory -> detailPersistencePort.listByInventoryId(inventory.getId())
                        .collectList()
                        .map(details -> {
                            inventory.setDetails(details);
                            return inventory;
                        }));
    }

    // ---------------------------------------------------------------
    // START INVENTORY
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public Mono<PhysicalInventory> startInventory(UUID id) {
        return Mono.zip(persistencePort.findById(id), getAuthenticatedUserId())
                .switchIfEmpty(Mono.error(new BadRequestException("Inventario no encontrado o usuario no autenticado")))
                .flatMap(tuple -> {
                    PhysicalInventory existing = tuple.getT1();
                    UUID userId = tuple.getT2();
                    if (!"PLANNED".equals(existing.getInventoryStatus()))
                        return Mono.error(new BadRequestException("Solo se puede iniciar inventarios en estado PLANNED"));

                    existing.setInventoryStatus("IN_PROCESS");
                    existing.setActualStartDate(LocalDateTime.now());
                    existing.setUpdatedBy(userId);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return persistencePort.save(existing);
                });
    }

    // ---------------------------------------------------------------
    // COMPLETE INVENTORY
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public Mono<PhysicalInventory> completeInventory(UUID id) {
        return Mono.zip(persistencePort.findById(id), getAuthenticatedUserId())
                .switchIfEmpty(Mono.error(new BadRequestException("Inventario no encontrado o usuario no autenticado")))
                .flatMap(tuple -> {
                    PhysicalInventory existing = tuple.getT1();
                    UUID userId = tuple.getT2();
                    if (!"IN_PROCESS".equals(existing.getInventoryStatus()))
                        return Mono.error(new BadRequestException("Solo se puede completar inventarios en estado IN_PROCESS"));

                    return detailPersistencePort.listByInventoryId(id)
                            .collectList()
                            .flatMap(details -> {
                                boolean faltantes = details.stream().anyMatch(d -> d.getVerifiedBy() == null);
                                if (faltantes)
                                    return Mono.error(new BadRequestException("No se puede completar: hay bienes sin verificar"));

                                existing.setInventoryStatus("COMPLETED");
                                existing.setActualEndDate(LocalDateTime.now());
                                existing.setProgressPercentage(100.0);
                                existing.setUpdatedBy(userId);
                                existing.setUpdatedAt(LocalDateTime.now());
                                return persistencePort.save(existing);
                            });
                });
    }

    // ---------------------------------------------------------------
    // GET FORM DATA
    // ---------------------------------------------------------------
    @Override
    public Mono<InventoryFormDataDTO> getFormData() {
        log.warn("========== INICIANDO getFormData() ==========");
        return Mono.zip(
                configurationClientPort.getAreas()
                        .filter(a -> "A".equals(a.getStatus()) || "ACTIVE".equalsIgnoreCase(a.getStatus()))
                        .collectList(),
                configurationClientPort.getCategories()
                        .filter(c -> "A".equals(c.getStatus()) || "ACTIVE".equalsIgnoreCase(c.getStatus()))
                        .collectList(),
                configurationClientPort.getLocations()
                        .filter(l -> "A".equals(l.getStatus()) || "ACTIVE".equalsIgnoreCase(l.getStatus()))
                        .collectList(),
                userClientPort.getUsers()
                        .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                        .collectList())
                .map(t -> new InventoryFormDataDTO(t.getT1(), t.getT2(), t.getT3(), t.getT4()));
    }

    // ---------------------------------------------------------------
    // HELPERS PRIVADOS
    // ---------------------------------------------------------------
    private boolean isActiveAsset(AssetDTO asset) {
        String status = asset.getConservationStatus();
        return status != null
                && !status.equalsIgnoreCase("BAJA")
                && !status.equalsIgnoreCase("CANCELLED");
    }

    private void validateInventoryRules(PhysicalInventory inventory) {
        switch (inventory.getInventoryType()) {
            case "GENERAL":
                if (inventory.getAreaId() != null || inventory.getCategoryId() != null
                        || inventory.getLocationId() != null)
                    throw new BadRequestException(
                            "Los inventarios GENERAL no deben tener filtros (área/categoría/ubicación).");
                break;
            case "SELECTIVE":
                int filters = 0;
                if (inventory.getAreaId() != null)
                    filters++;
                if (inventory.getCategoryId() != null)
                    filters++;
                if (inventory.getLocationId() != null)
                    filters++;
                if (filters == 0)
                    throw new BadRequestException("Los inventarios SELECTIVE deben incluir al menos un filtro.");
                if (filters > 1)
                    throw new BadRequestException("Los inventarios SELECTIVE solo pueden tener un filtro a la vez.");
                break;
            case "SPECIAL":
            case "RECONCILIATION":
                break;
            default:
                throw new BadRequestException("Tipo de inventario inválido.");
        }
    }
}
