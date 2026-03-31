package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AssetDisposalDetailRequest {
    private UUID municipalityId;
    private UUID disposalId;
    private UUID assetId;
    private String conservationStatus;
    private BigDecimal bookValue;
    private BigDecimal recoverableValue;
    private String observations;
    private String conditionPhotographs; // JSON array as string
}
