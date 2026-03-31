package pe.edu.vallegrande.ms_maintenanceService.domain.model;

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
public class MaintenanceConformity {
    private UUID id;
    private UUID maintenanceId;
    private UUID municipalityId;
    
    private String conformityNumber;
    private String workQuality;
    private String assetConditionAfter;
    private String observations;
    
    private UUID confirmedBy;
    private LocalDateTime confirmedAt;
    private String digitalSignature;
    
    private Boolean requiresFollowup;
    private String followupDescription;
    private LocalDateTime createdAt;
}
