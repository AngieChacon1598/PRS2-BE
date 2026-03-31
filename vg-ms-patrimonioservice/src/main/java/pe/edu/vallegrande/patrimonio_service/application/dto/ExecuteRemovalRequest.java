package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ExecuteRemovalRequest {
    private UUID removalResponsibleId;
    private String finalDestination;
    private String observations;
}
