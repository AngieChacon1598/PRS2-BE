package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AssetDisposalDetailResponse {
    private UUID id;
    private UUID municipalityId;
    private UUID disposalId;
    private UUID assetId;
    private String conservationStatus;
    private BigDecimal bookValue;
    private BigDecimal recoverableValue;
    private String technicalOpinion;
    private String recommendation;
    private LocalDate removalDate;
    private UUID removalResponsibleId;
    private String finalDestination;
    private String observations;
    private String conditionPhotographs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
