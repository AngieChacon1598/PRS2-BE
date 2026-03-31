package pe.edu.vallegrande.patrimonio_service.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("depreciations")
public class Depreciation {
    @Id
    private UUID id;
    
    // Basic Asset Data
    private UUID assetId;
    
    // Calculation Period
    private Integer fiscalYear;
    private Integer calculationMonth;
    
    // Calculation Values
    private BigDecimal initialValue;
    private Integer usefulLifeYears;
    private BigDecimal residualValue;
    
    // Calculated Depreciation
    private BigDecimal annualDepreciation;
    private BigDecimal monthlyDepreciation;
    private BigDecimal previousAccumulatedDepreciation;
    private BigDecimal periodDepreciation;
    private BigDecimal currentAccumulatedDepreciation;
    
    // Book Value
    private BigDecimal previousBookValue;
    private BigDecimal currentBookValue;
    
    // Calculation Status
    private String calculationStatus;
    private String depreciationMethod;
    
    // Observations
    private String observations;
    
    // Basic Audit
    private UUID calculatedBy;
    private UUID approvedBy;
    private LocalDateTime calculationDate;
    private LocalDateTime approvalDate;
}
