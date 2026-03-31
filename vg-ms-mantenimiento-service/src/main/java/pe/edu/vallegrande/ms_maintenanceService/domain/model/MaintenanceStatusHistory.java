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
public class MaintenanceStatusHistory {
    private UUID id;
    private UUID maintenanceId;
    private UUID municipalityId;
    
    private String previousStatus;
    private String newStatus;
    private String reason;
    
    private UUID changedBy;
    private LocalDateTime changedAt;
}
