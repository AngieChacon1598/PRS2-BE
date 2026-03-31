package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DepreciationResponse {
    private UUID id;
    private UUID assetId;
    private Integer fiscalYear;
    private Integer calculationMonth;
    private BigDecimal initialValue;
    private Integer usefulLifeYears;
    private BigDecimal residualValue;
    private BigDecimal annualDepreciation;
    private BigDecimal monthlyDepreciation;
    private BigDecimal previousAccumulatedDepreciation;
    private BigDecimal periodDepreciation;
    private BigDecimal currentAccumulatedDepreciation;
    private BigDecimal previousBookValue;
    private BigDecimal currentBookValue;
    private String calculationStatus;
    private String depreciationMethod;
    private String observations;
    private UUID calculatedBy;
    private UUID approvedBy;
    private LocalDateTime calculationDate;
    private LocalDateTime approvalDate;
}
