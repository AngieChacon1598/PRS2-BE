package pe.edu.vallegrande.ms_inventory.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("physical_inventory_detail")
@Schema(description = "Detalle del inventario físico")
public class PhysicalInventoryDetail {

     @Id
     @Schema(description = "ID del detalle", example = "123e4567-e89b-12d3-a456-426614174100")
     private UUID id;

     private UUID municipalityId;
     private UUID inventoryId;
     private UUID assetId;

     private String foundStatus;
     private String actualConservationStatus;
     private UUID actualLocationId;
     private UUID actualResponsibleId;

     private UUID verifiedBy;
     @Schema(description = "Fecha de verificación", type = "string", format = "date-time")
     private LocalDateTime verificationDate;

     private String observations;
     private Boolean requiresAction;
     private String requiredAction;

     private JsonNode photographs;
     private JsonNode additionalEvidence;

     private String physicalDifferences;
     private String documentDifferences;

     @Schema(description = "Fecha de creación", type = "string", format = "date-time")
     private LocalDateTime createdAt;

     @Schema(description = "Fecha de actualización", type = "string", format = "date-time")
     private LocalDateTime updatedAt;

     @org.springframework.data.annotation.Transient
     private Boolean active = true; // true = activo, false = borrado
     @org.springframework.data.annotation.Transient
     private LocalDateTime deletedAt;
}