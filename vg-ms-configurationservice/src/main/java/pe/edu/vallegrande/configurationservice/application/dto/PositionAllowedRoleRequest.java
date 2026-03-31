package pe.edu.vallegrande.configurationservice.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PositionAllowedRoleRequest {

    @NotNull(message = "positionId es requerido")
    private UUID positionId;

    private UUID areaId;

    @NotNull(message = "roleId es requerido")
    private UUID roleId;

    private Boolean isDefault = false;

    @NotNull(message = "municipalityId es requerido")
    private UUID municipalityId;
}
