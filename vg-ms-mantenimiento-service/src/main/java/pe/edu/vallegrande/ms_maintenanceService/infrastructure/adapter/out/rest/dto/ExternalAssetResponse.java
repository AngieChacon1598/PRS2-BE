package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class ExternalAssetResponse {
    private UUID id;
    private String assetCode;
    private String description;
}
