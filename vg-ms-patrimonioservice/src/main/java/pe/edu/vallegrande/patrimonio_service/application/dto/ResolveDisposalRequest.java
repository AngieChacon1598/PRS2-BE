package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ResolveDisposalRequest {
    private Boolean approved;
    private String resolutionNumber;
    private String observations;
    private UUID approvedById; // Nuevo: quien aprueba (administrador de finanzas)
}
