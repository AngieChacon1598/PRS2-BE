package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.vallegrande.ms_maintenanceService.application.dto.*;
import pe.edu.vallegrande.ms_maintenanceService.application.mapper.MaintenanceMapper;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.in.MaintenanceServicePort;
import pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.in.rest.dto.MaintenanceWebRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceServicePort maintenanceService;
    private final MaintenanceMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('mantenimiento:create') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> create(
            @Valid @RequestBody MaintenanceWebRequest webRequest,
            @AuthenticationPrincipal Jwt jwt) {

        UUID municipalityId = getMunicipalityId(jwt, webRequest.getMunicipalityId());
        UUID requestedBy = getUserId(jwt, webRequest.getRequestedBy());

        MaintenanceRequestDTO applicationDTO = MaintenanceRequestDTO.builder()
                .municipalityId(municipalityId)
                .maintenanceCode(webRequest.getMaintenanceCode())
                .assetId(webRequest.getAssetId())
                .maintenanceType(webRequest.getMaintenanceType())
                .priority(webRequest.getPriority())
                .scheduledDate(webRequest.getScheduledDate())
                .workDescription(webRequest.getWorkDescription())
                .reportedProblem(webRequest.getReportedProblem())
                .observations(webRequest.getObservations())
                .technicalResponsibleId(webRequest.getTechnicalResponsibleId())
                .serviceSupplierId(webRequest.getServiceSupplierId())
                .supervisorId(webRequest.getSupervisorId())
                .laborCost(webRequest.getLaborCost())
                .additionalCost(webRequest.getAdditionalCost())
                .hasWarranty(webRequest.getHasWarranty())
                .warrantyExpirationDate(webRequest.getWarrantyExpirationDate())
                .requestedBy(requestedBy)
                .build();

        return maintenanceService.create(mapper.toEntity(applicationDTO))
                .map(m -> ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponseDTO(m)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('mantenimiento:read') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> findById(@PathVariable("id") UUID id) {
        return maintenanceService.findById(id)
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('mantenimiento:read') or hasRole('TENANT_ADMIN')")
    public Flux<MaintenanceResponseDTO> findAll(@AuthenticationPrincipal Jwt jwt) {
        return maintenanceService.findAll(getMunicipalityId(jwt, null))
                .map(mapper::toResponseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('mantenimiento:update') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MaintenanceWebRequest webRequest,
            @AuthenticationPrincipal Jwt jwt) {

        MaintenanceRequestDTO applicationDTO = MaintenanceRequestDTO.builder()
                .municipalityId(getMunicipalityId(jwt, webRequest.getMunicipalityId()))
                .maintenanceCode(webRequest.getMaintenanceCode())
                .assetId(webRequest.getAssetId())
                .maintenanceType(webRequest.getMaintenanceType())
                .priority(webRequest.getPriority())
                .scheduledDate(webRequest.getScheduledDate())
                .workDescription(webRequest.getWorkDescription())
                .reportedProblem(webRequest.getReportedProblem())
                .observations(webRequest.getObservations())
                .technicalResponsibleId(webRequest.getTechnicalResponsibleId())
                .serviceSupplierId(webRequest.getServiceSupplierId())
                .supervisorId(webRequest.getSupervisorId())
                .laborCost(webRequest.getLaborCost())
                .additionalCost(webRequest.getAdditionalCost())
                .hasWarranty(webRequest.getHasWarranty())
                .warrantyExpirationDate(webRequest.getWarrantyExpirationDate())
                .build();

        return maintenanceService.update(id, mapper.toEntity(applicationDTO))
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    // --- Flujo de Estados SBN ---

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAuthority('mantenimiento:update') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> start(
            @PathVariable("id") UUID id,
            @Valid @RequestBody StartMaintenanceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return maintenanceService.startMaintenance(id, getUserId(jwt, request.getUpdatedBy()), request.getObservations())
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('mantenimiento:close') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> complete(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CompleteMaintenanceRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return maintenanceService.completeMaintenance(
                        id,
                        request.getWorkOrder(),
                        request.getLaborCost(),
                        request.getAppliedSolution(),
                        request.getObservations(),
                        getUserId(jwt, request.getUpdatedBy()))
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('mantenimiento:confirm') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> confirm(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MaintenanceConformityRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        MaintenanceConformity conformity = MaintenanceConformity.builder()
                .maintenanceId(id)
                .municipalityId(getMunicipalityId(jwt, null))
                .conformityNumber(request.getConformityNumber())
                .workQuality(request.getWorkQuality())
                .assetConditionAfter(request.getAssetConditionAfter())
                .observations(request.getObservations())
                .confirmedBy(getUserId(jwt, request.getConfirmedBy()))
                .build();

        return maintenanceService.confirmMaintenance(id, conformity)
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('mantenimiento:update') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> suspend(
            @PathVariable("id") UUID id,
            @Valid @RequestBody SuspendMaintenanceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return maintenanceService.suspendMaintenance(id, request.getNextDate(), request.getObservations(), getUserId(jwt, request.getUpdatedBy()))
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('mantenimiento:update') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenanceResponseDTO>> cancel(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CancelMaintenanceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return maintenanceService.cancelMaintenanceWithReason(id, request.getObservations(), getUserId(jwt, request.getUpdatedBy()))
                .map(m -> ResponseEntity.ok(mapper.toResponseDTO(m)));
    }

    // --- Gestión de Detalles (Repuestos e Historial) ---

    @PostMapping("/{id}/parts")
    @PreAuthorize("hasAuthority('mantenimiento:update') or hasRole('TENANT_ADMIN')")
    public Mono<ResponseEntity<MaintenancePart>> addPart(
            @PathVariable("id") UUID id,
            @Valid @RequestBody MaintenancePartRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        MaintenancePart part = MaintenancePart.builder()
                .maintenanceId(id)
                .municipalityId(getMunicipalityId(jwt, null))
                .partName(request.getPartName())
                .description(request.getDescription())
                .partType(request.getPartType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitCost())
                .unitOfMeasure("UND") // Default
                .build();

        return maintenanceService.addPart(id, part)
                .map(p -> ResponseEntity.status(HttpStatus.CREATED).body(p));
    }

    @GetMapping("/{id}/parts")
    @PreAuthorize("hasAuthority('mantenimiento:read') or hasRole('TENANT_ADMIN')")
    public Flux<MaintenancePart> getParts(@PathVariable("id") UUID id) {
        return maintenanceService.getParts(id);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('mantenimiento:read') or hasRole('TENANT_ADMIN')")
    public Flux<MaintenanceStatusHistoryResponseDTO> getHistory(@PathVariable("id") UUID id) {
        return maintenanceService.getHistory(id)
                .map(h -> MaintenanceStatusHistoryResponseDTO.builder()
                        .maintenanceId(h.getMaintenanceId())
                        .previousStatus(h.getPreviousStatus())
                        .newStatus(h.getNewStatus())
                        .reason(h.getReason())
                        .changedBy(h.getChangedBy())
                        .changedAt(h.getChangedAt())
                        .build());
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('mantenimiento:read') or hasRole('TENANT_ADMIN')")
    public Flux<MaintenanceResponseDTO> findByStatus(
            @RequestParam("status") String status,
            @AuthenticationPrincipal Jwt jwt) {
        return maintenanceService.findByStatus(status, getMunicipalityId(jwt, null))
                .map(mapper::toResponseDTO);
    }

    // --- Helpers ---

    private UUID getMunicipalityId(Jwt jwt, UUID defaultValue) {
        String municipalCode = jwt.getClaimAsString("municipal_code");
        return (municipalCode != null && municipalCode.length() == 36) ? UUID.fromString(municipalCode) : defaultValue;
    }

    private UUID getUserId(Jwt jwt, UUID defaultValue) {
        String userId = jwt.getClaimAsString("user_id");
        return (userId != null && userId.length() == 36) ? UUID.fromString(userId) : defaultValue;
    }
}
