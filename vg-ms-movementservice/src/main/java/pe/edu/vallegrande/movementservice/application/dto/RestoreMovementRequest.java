package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestoreMovementRequest {

    @NotNull(message = "restoredBy es obligatorio")
    private UUID restoredBy;
}
