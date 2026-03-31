package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectMovementRequest {

    @NotNull(message = "approvedBy es obligatorio")
    private UUID approvedBy;

    @Size(max = 2000, message = "rejectionReason no puede superar 2000 caracteres")
    private String rejectionReason;
}
