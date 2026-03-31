package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenancePartRequest {
    @NotBlank(message = "El nombre del repuesto es obligatorio")
    private String partName;
    
    private String description;
    
    @NotBlank(message = "El tipo de parte es obligatorio")
    private String partType; // SPARE_PART, CONSUMABLE, TOOL, SERVICE
    
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01")
    private BigDecimal quantity;
    
    @NotNull(message = "El costo unitario es obligatorio")
    @DecimalMin(value = "0.0")
    private BigDecimal unitCost;
}
