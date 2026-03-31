package pe.edu.vallegrande.ms_inventory.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
@Table("physical_inventories")
public class PhysicalInventory {

     @Id
     private UUID id;

     private UUID municipalityId;
     private String inventoryNumber;
     private String inventoryType;
     private String description;
     private UUID areaId;
     private UUID categoryId;
     private UUID locationId;
     private LocalDate plannedStartDate;
     private LocalDate plannedEndDate;
     private LocalDateTime actualStartDate;
     private LocalDateTime actualEndDate;
     private String inventoryStatus;
     private Double progressPercentage;
     private UUID generalResponsibleId;
     private JsonNode inventoryTeam; // JSONB
     private Boolean includesMissing;
     private Boolean includesSurplus;
     private Boolean requiresPhotos;
     private String observations;
     private JsonNode attachedDocuments; // JSONB
     private UUID createdBy;
     private LocalDateTime createdAt;
     private UUID updatedBy;
     private LocalDateTime updatedAt;

     @org.springframework.data.annotation.Transient
     private List<PhysicalInventoryDetail> details;

     public List<PhysicalInventoryDetail> getDetails() {
          return details;
     }

     public void setDetails(List<PhysicalInventoryDetail> details) {
          this.details = details;
     }
}
