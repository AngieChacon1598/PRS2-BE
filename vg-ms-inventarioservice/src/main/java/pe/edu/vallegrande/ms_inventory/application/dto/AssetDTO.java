package pe.edu.vallegrande.ms_inventory.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AssetDTO {
    private UUID id;
    private String assetCode;
    private String description;
    private String conservationStatus;
    private UUID currentLocationId;
    private UUID currentResponsibleId;
    private UUID currentAreaId;
    private UUID categoryId;
}
