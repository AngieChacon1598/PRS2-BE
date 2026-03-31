package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AssignCommitteeRequest {
    private UUID assignedBy; // Usuario que inicia la evaluación técnica
}
