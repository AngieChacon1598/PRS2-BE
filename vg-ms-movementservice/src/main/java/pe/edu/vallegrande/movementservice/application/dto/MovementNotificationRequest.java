package pe.edu.vallegrande.movementservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementNotificationRequest {

    private UUID movementId;
    private UUID assetId;
    private UUID municipalityId;
    private String movementNumber;
    private String movementType;
    private String movementStatus; 
    private UUID requestingUser;
    private String reason;
    private LocalDateTime requestDate;
    
    private UUID originResponsibleId;
    private UUID destinationResponsibleId;
    private UUID originAreaId;
    private UUID destinationAreaId;
    private UUID originLocationId;
    private UUID destinationLocationId;
}
