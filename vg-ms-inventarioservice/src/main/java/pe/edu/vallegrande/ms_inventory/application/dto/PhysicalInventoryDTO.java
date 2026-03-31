package pe.edu.vallegrande.ms_inventory.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data

public class PhysicalInventoryDTO {

    private UUID id;
    private UUID municipalityId;
    private String inventoryNumber;
    private String inventoryType;
    private String description;
    private UUID areaId;
    private UUID categoryId;
    private UUID locationId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private UUID generalResponsibleId;
    private Boolean includesMissing;
    private Boolean includesSurplus;
    private Boolean requiresPhotos;
    private String observations;
    private JsonNode inventoryTeam;
    private JsonNode attachedDocuments;
}
