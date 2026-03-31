package pe.edu.vallegrande.ms_maintenanceService.application.dto;

import java.util.UUID;

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
public class MaintenanceConformityRequest {
    private String conformityNumber;
    
    @NotBlank(message = "La calidad del trabajo es obligatoria (EXCELLENT, GOOD, etc.)")
    private String workQuality;
    
    @NotBlank(message = "El estado del bien tras el mantenimiento es obligatorio")
    private String assetConditionAfter;
    
    private String observations;
    
    @NotNull(message = "El ID de quien confirma es obligatorio")
    private UUID confirmedBy;
}
