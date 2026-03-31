package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
        regexp = "SCHEDULED|IN_PROCESS|COMPLETED|CANCELLED|SUSPENDED",
        message = "Estado inválido. Valores permitidos: SCHEDULED, IN_PROCESS, COMPLETED, CANCELLED, SUSPENDED"
    )
    private String status;

    @NotNull(message = "El ID del usuario que actualiza es obligatorio")
    private UUID updatedBy;
}

