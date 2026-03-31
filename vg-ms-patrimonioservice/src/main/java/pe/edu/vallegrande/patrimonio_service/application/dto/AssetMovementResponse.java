package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AssetMovementResponse {

    private UUID id;
    private String movementNumber;
    private String movementStatus;
}
