package pe.edu.vallegrande.patrimonio_service.application.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DepreciationRequest {
    private UUID assetId;
    private Integer fiscalYear;
    private Integer calculationMonth;
    private BigDecimal initialValue;
    private Integer usefulLifeYears;
    private BigDecimal residualValue;
    private String depreciationMethod;
    private String observations;
}
