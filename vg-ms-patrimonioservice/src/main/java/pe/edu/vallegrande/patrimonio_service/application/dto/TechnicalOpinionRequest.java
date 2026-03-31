package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;

@Data
public class TechnicalOpinionRequest {
    private String technicalOpinion;
    private String recommendation; // DESTROY, DONATE, SELL, RECYCLE, TRANSFER
    private String observations;
}
