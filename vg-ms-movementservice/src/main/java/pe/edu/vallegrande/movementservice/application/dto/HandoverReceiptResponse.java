package pe.edu.vallegrande.movementservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverReceiptResponse {
    private UUID id;
    private UUID municipalityId;
    private String receiptNumber;
    private UUID movementId;
    private UUID deliveringResponsibleId;
    private UUID receivingResponsibleId;
    private UUID witness1Id;
    private UUID witness2Id;
    private LocalDate receiptDate;
    private LocalDateTime deliverySignatureDate;
    private LocalDateTime receptionSignatureDate;
    private String receiptStatus;
    private String deliveryObservations;
    private String receptionObservations;
    private String specialConditions;
    private String digitalSignatures;
    private UUID generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String deliveringResponsibleName;
    private String receivingResponsibleName;
    private String witness1Name;
    private String witness2Name;
    private String generatedByName;
}
