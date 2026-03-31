package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.time.LocalDate;
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

public class RescheduleMaintenanceRequest {

    @NotNull(message = "La próxima fecha es obligatoria")
    private LocalDate nextDate;


    private UUID technicalResponsibleId;


    private UUID serviceSupplierId;

    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID updatedBy;

    private String observations;
}

