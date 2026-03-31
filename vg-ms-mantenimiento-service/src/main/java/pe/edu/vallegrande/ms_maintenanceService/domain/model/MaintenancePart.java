package pe.edu.vallegrande.ms_maintenanceService.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePart {
    private UUID id;
    private UUID maintenanceId;
    private UUID municipalityId;
    
    private String partCode;
    private String partName;
    private String description;
    private String partType;
    
    private BigDecimal quantity;
    private String unitOfMeasure;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    private UUID supplierId;
    private LocalDateTime createdAt;
}
