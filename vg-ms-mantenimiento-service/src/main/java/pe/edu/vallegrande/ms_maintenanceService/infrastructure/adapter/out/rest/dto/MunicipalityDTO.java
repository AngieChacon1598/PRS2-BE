package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class MunicipalityDTO {
    private UUID id;
    private String ubigeoCode;
}
