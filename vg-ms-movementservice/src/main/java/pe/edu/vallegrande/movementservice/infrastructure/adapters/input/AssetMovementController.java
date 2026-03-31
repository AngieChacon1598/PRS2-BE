package pe.edu.vallegrande.movementservice.infrastructure.adapters.input;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import pe.edu.vallegrande.movementservice.application.dto.ApproveMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.CancelMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.DeleteMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.InProcessMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.RejectMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.RestoreMovementRequest;
import pe.edu.vallegrande.movementservice.application.validation.ValidationGroups;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementRequest;
import pe.edu.vallegrande.movementservice.application.dto.AssetMovementResponse;
import pe.edu.vallegrande.movementservice.application.ports.input.AssetMovementServicePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/asset-movements")
@RequiredArgsConstructor
@Tag(name = "Asset Movements", description = "API for asset movements management with complete traceability by municipality")
public class AssetMovementController {

    private final AssetMovementServicePort assetMovementService;

    @PostMapping
    @Operation(summary = "Create asset movement", 
               description = "Creates a new asset movement with traceability by municipality")
    public Mono<ResponseEntity<AssetMovementResponse>> create(
            @Validated({Default.class, ValidationGroups.OnCreate.class}) @Valid @RequestBody AssetMovementRequest request) {
        return assetMovementService.create(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/municipality/{municipalityId}")
    @Operation(summary = "Get all movements by municipality", 
               description = "Gets all asset movements for a specific municipality")
    public Flux<AssetMovementResponse> getAllByMunicipality(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getAllByMunicipality(municipalityId);
    }

    @GetMapping("/{id}/municipality/{municipalityId}")
    @Operation(summary = "Get movement by ID", 
               description = "Gets a specific asset movement by its ID and municipality")
    public Mono<ResponseEntity<AssetMovementResponse>> getById(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getById(id, municipalityId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/municipality/{municipalityId}")
    @Operation(summary = "Update movement", 
               description = "Updates an existing asset movement")
    public Mono<ResponseEntity<AssetMovementResponse>> update(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody AssetMovementRequest request) {
        return assetMovementService.update(id, municipalityId, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping(value = "/{id}/municipality/{municipalityId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Soft delete movement", 
               description = "Soft deletes (logically deletes) an asset movement. Requires 'deletedBy' in request body.")
    public Mono<ResponseEntity<AssetMovementResponse>> delete(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody DeleteMovementRequest request) {
        return assetMovementService.delete(id, municipalityId, request.getDeletedBy())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/asset/{assetId}/municipality/{municipalityId}")
    @Operation(summary = "Get movements by asset", 
               description = "Gets all movements for a specific asset ordered by request date descending")
    public Flux<AssetMovementResponse> getByAsset(
            @Parameter(description = "Asset ID") @PathVariable UUID assetId,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getByAsset(assetId, municipalityId);
    }

    @GetMapping("/type/{movementType}/municipality/{municipalityId}")
    @Operation(summary = "Get movements by type", 
               description = "Gets all movements of a specific type (INITIAL_ASSIGNMENT, REASSIGNMENT, etc.)")
    public Flux<AssetMovementResponse> getByMovementType(
            @Parameter(description = "Movement type") @PathVariable String movementType,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getByMovementType(movementType, municipalityId);
    }

    @GetMapping("/status/{status}/municipality/{municipalityId}")
    @Operation(summary = "Get movements by status", 
               description = "Gets all movements with a specific status (REQUESTED, APPROVED, etc.)")
    public Flux<AssetMovementResponse> getByStatus(
            @Parameter(description = "Movement status") @PathVariable String status,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getByStatus(status, municipalityId);
    }

    @GetMapping("/pending-approval/municipality/{municipalityId}")
    @Operation(summary = "Get movements pending approval", 
               description = "Gets all movements pending approval for a municipality")
    public Flux<AssetMovementResponse> getPendingApproval(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getPendingApproval(municipalityId);
    }

    @PostMapping("/{id}/approve/municipality/{municipalityId}")
    @Operation(summary = "Approve movement", 
               description = "Approves a pending movement")
    public Mono<ResponseEntity<AssetMovementResponse>> approve(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody ApproveMovementRequest request) {
        return assetMovementService.approve(id, municipalityId, request.getApprovedBy())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject/municipality/{municipalityId}")
    @Operation(summary = "Reject movement", 
               description = "Rejects a pending movement")
    public Mono<ResponseEntity<AssetMovementResponse>> reject(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody RejectMovementRequest request) {
        return assetMovementService.reject(id, municipalityId, request.getApprovedBy(), request.getRejectionReason())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/in-process/municipality/{municipalityId}")
    @Operation(summary = "Mark movement as in process", 
               description = "Marks an approved movement as in process")
    public Mono<ResponseEntity<AssetMovementResponse>> markInProcess(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody InProcessMovementRequest request) {
        return assetMovementService.markInProcess(id, municipalityId, request.getExecutingUser())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/complete/municipality/{municipalityId}")
    @Operation(summary = "Complete movement", 
               description = "Marks a movement as completed")
    public Mono<ResponseEntity<AssetMovementResponse>> complete(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.complete(id, municipalityId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel/municipality/{municipalityId}")
    @Operation(summary = "Cancel movement", 
               description = "Cancels a movement")
    public Mono<ResponseEntity<AssetMovementResponse>> cancel(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody(required = false) CancelMovementRequest request) {
        String cancellationReason = request != null ? request.getCancellationReason() : null;
        return assetMovementService.cancel(id, municipalityId, cancellationReason)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/destination-responsible/{destinationResponsibleId}/municipality/{municipalityId}")
    @Operation(summary = "Get movements by destination responsible", 
               description = "Gets all movements for a specific destination responsible")
    public Flux<AssetMovementResponse> getByDestinationResponsible(
            @Parameter(description = "Destination responsible user ID") @PathVariable UUID destinationResponsibleId,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getByDestinationResponsible(destinationResponsibleId, municipalityId);
    }

    @GetMapping("/origin-responsible/{originResponsibleId}/municipality/{municipalityId}")
    @Operation(summary = "Get movements by origin responsible", 
               description = "Gets all movements for a specific origin responsible")
    public Flux<AssetMovementResponse> getByOriginResponsible(
            @Parameter(description = "Origin responsible user ID") @PathVariable UUID originResponsibleId,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getByOriginResponsible(originResponsibleId, municipalityId);
    }

    @GetMapping("/count/municipality/{municipalityId}")
    @Operation(summary = "Count movements", 
               description = "Gets the count of active movements for a municipality")
    public Mono<ResponseEntity<Map<String, Long>>> countByMunicipality(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.countByMunicipality(municipalityId)
                .map(count -> ResponseEntity.ok(Map.of("count", count)));
    }

    @GetMapping("/deleted/municipality/{municipalityId}")
    @Operation(summary = "Get deleted movements", 
               description = "Gets all soft-deleted (inactive) movements for a municipality")
    public Flux<AssetMovementResponse> getDeletedByMunicipality(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return assetMovementService.getDeletedByMunicipality(municipalityId);
    }

    @PostMapping(value = "/{id}/restore/municipality/{municipalityId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Restore movement", 
               description = "Restores a soft-deleted asset movement. Requires 'restoredBy' in request body.")
    public Mono<ResponseEntity<AssetMovementResponse>> restore(
            @Parameter(description = "Movement ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody RestoreMovementRequest request) {
        return assetMovementService.restore(id, municipalityId, request.getRestoredBy())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
