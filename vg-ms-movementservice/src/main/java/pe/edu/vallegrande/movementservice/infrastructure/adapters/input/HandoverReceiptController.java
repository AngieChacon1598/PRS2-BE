package pe.edu.vallegrande.movementservice.infrastructure.adapters.input;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptRequest;
import pe.edu.vallegrande.movementservice.application.dto.HandoverReceiptResponse;
import pe.edu.vallegrande.movementservice.application.dto.SignatureRequest;
import pe.edu.vallegrande.movementservice.application.ports.input.HandoverReceiptServicePort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/handover-receipts")
@RequiredArgsConstructor
@Tag(name = "Handover Receipts", description = "API for managing handover receipts")
public class HandoverReceiptController {

    private final HandoverReceiptServicePort handoverReceiptService;

    @PostMapping("/municipality/{municipalityId}")
    @Operation(summary = "Create handover receipt", description = "Creates a new handover receipt for a movement")
    public Mono<ResponseEntity<HandoverReceiptResponse>> createHandoverReceipt(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody HandoverReceiptRequest request) {
        return handoverReceiptService.createHandoverReceipt(municipalityId, request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @PutMapping("/{id}/municipality/{municipalityId}")
    @Operation(summary = "Update handover receipt", description = "Updates an existing handover receipt")
    public Mono<ResponseEntity<HandoverReceiptResponse>> updateHandoverReceipt(
            @Parameter(description = "Handover receipt ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody HandoverReceiptRequest request) {
        return handoverReceiptService.updateHandoverReceipt(id, municipalityId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/municipality/{municipalityId}")
    @Operation(summary = "Get handover receipt by ID", description = "Retrieves a handover receipt by its ID")
    public Mono<ResponseEntity<HandoverReceiptResponse>> getHandoverReceiptById(
            @Parameter(description = "Handover receipt ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.getHandoverReceiptById(id, municipalityId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/municipality/{municipalityId}")
    @Operation(summary = "Get all handover receipts", description = "Retrieves all handover receipts for a municipality")
    public Flux<HandoverReceiptResponse> getAllHandoverReceipts(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.getAllHandoverReceipts(municipalityId);
    }

    @GetMapping("/movement/{movementId}/municipality/{municipalityId}")
    @Operation(summary = "Get handover receipt by movement", description = "Retrieves handover receipt for a specific movement")
    public Mono<ResponseEntity<HandoverReceiptResponse>> getHandoverReceiptByMovement(
            @Parameter(description = "Movement ID") @PathVariable UUID movementId,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.getHandoverReceiptByMovement(movementId, municipalityId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/status/{status}/municipality/{municipalityId}")
    @Operation(summary = "Get handover receipts by status", description = "Retrieves handover receipts by status")
    public Flux<HandoverReceiptResponse> getHandoverReceiptsByStatus(
            @Parameter(description = "Receipt status") @PathVariable String status,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.getHandoverReceiptsByStatus(status, municipalityId);
    }

    @GetMapping("/responsible/{responsibleId}/municipality/{municipalityId}")
    @Operation(summary = "Get handover receipts by responsible", description = "Retrieves handover receipts by responsible person")
    public Flux<HandoverReceiptResponse> getHandoverReceiptsByResponsible(
            @Parameter(description = "Responsible person ID") @PathVariable UUID responsibleId,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.getHandoverReceiptsByResponsible(responsibleId, municipalityId);
    }

    @PostMapping("/{id}/sign/municipality/{municipalityId}")
    @Operation(summary = "Sign handover receipt", description = "Signs a handover receipt (delivery or reception)")
    public Mono<ResponseEntity<HandoverReceiptResponse>> signHandoverReceipt(
            @Parameter(description = "Handover receipt ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId,
            @Valid @RequestBody SignatureRequest request) {
        return handoverReceiptService.signHandoverReceipt(id, municipalityId, request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/count/municipality/{municipalityId}")
    @Operation(summary = "Count handover receipts", description = "Counts total handover receipts for a municipality")
    public Mono<ResponseEntity<Long>> countHandoverReceipts(
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.countHandoverReceipts(municipalityId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/count/status/{status}/municipality/{municipalityId}")
    @Operation(summary = "Count handover receipts by status", description = "Counts handover receipts by status")
    public Mono<ResponseEntity<Long>> countHandoverReceiptsByStatus(
            @Parameter(description = "Receipt status") @PathVariable String status,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.countHandoverReceiptsByStatus(municipalityId, status)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/void/municipality/{municipalityId}")
    @Operation(summary = "Void handover receipt", description = "Marks a handover receipt as VOIDED (no longer valid)")
    public Mono<ResponseEntity<HandoverReceiptResponse>> voidHandoverReceipt(
            @Parameter(description = "Handover receipt ID") @PathVariable UUID id,
            @Parameter(description = "Municipality ID") @PathVariable UUID municipalityId) {
        return handoverReceiptService.voidHandoverReceipt(id, municipalityId)
                .map(ResponseEntity::ok);
    }

}


