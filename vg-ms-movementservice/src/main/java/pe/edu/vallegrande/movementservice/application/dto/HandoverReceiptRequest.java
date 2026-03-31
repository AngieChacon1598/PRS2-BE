package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverReceiptRequest {

    @NotNull(message = "movementId es obligatorio")
    private UUID movementId;

    @NotNull(message = "deliveringResponsibleId es obligatorio")
    private UUID deliveringResponsibleId;

    @NotNull(message = "receivingResponsibleId es obligatorio")
    private UUID receivingResponsibleId;

    private UUID witness1Id;

    private UUID witness2Id;

    @PastOrPresent(message = "receiptDate no puede ser una fecha futura")
    private LocalDate receiptDate;

    @Size(max = 4000, message = "deliveryObservations no puede superar 4000 caracteres")
    private String deliveryObservations;

    @Size(max = 4000, message = "receptionObservations no puede superar 4000 caracteres")
    private String receptionObservations;

    @Size(max = 2000, message = "specialConditions no puede superar 2000 caracteres")
    private String specialConditions;

    @NotNull(message = "generatedBy es obligatorio")
    private UUID generatedBy;
}
