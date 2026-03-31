package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AssetDisposalRequest {
    private UUID municipalityId;
    private String disposalType;
    private String disposalReason;
    private String reasonDescription;
    private UUID technicalReportAuthorId; // Nuevo: quién elabora el informe técnico
    private String observations;
    private Boolean requiresDestruction;
    private Boolean allowsDonation;
    private BigDecimal recoverableValue;
    private UUID requestedBy;
}
