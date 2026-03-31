package pe.edu.vallegrande.movementservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateRequest {

    private String assetStatus; 
    private UUID currentResponsibleId;
    private UUID currentAreaId;
    private UUID currentLocationId;
    private String observations; 
}
