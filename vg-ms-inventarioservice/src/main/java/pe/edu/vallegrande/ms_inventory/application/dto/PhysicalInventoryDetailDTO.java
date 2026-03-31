package pe.edu.vallegrande.ms_inventory.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.UUID;

@Data
public class PhysicalInventoryDetailDTO {
    private UUID municipalityId;
    private UUID inventoryId;
    private UUID assetId;
    private String foundStatus;
    private String actualConservationStatus;
    private UUID actualLocationId;
    private UUID actualResponsibleId;
    private Boolean requiresAction;
    private String requiredAction;
    private JsonNode photographs;
    private JsonNode additionalEvidence;
}
