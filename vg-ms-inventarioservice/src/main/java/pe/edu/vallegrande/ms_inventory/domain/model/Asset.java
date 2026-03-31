package pe.edu.vallegrande.ms_inventory.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {
     private UUID id;
     private String assetCode;
     private String description;
     private String conservationStatus;
     private UUID currentLocationId;
     private UUID currentResponsibleId;
     private UUID currentAreaId;
     private UUID categoryId;
}