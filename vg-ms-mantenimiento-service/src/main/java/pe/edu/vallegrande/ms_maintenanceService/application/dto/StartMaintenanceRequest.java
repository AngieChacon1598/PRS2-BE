package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StartMaintenanceRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID updatedBy;

    private String observations;
}

