package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelMovementRequest {

    @Size(max = 2000, message = "cancellationReason no puede superar 2000 caracteres")
    private String cancellationReason;
}
