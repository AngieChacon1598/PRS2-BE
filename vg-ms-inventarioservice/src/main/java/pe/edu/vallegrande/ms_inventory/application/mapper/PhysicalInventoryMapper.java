package pe.edu.vallegrande.ms_inventory.application.mapper;

import pe.edu.vallegrande.ms_inventory.application.dto.PhysicalInventoryDTO;
import pe.edu.vallegrande.ms_inventory.domain.model.PhysicalInventory;

public class PhysicalInventoryMapper {

     public static PhysicalInventory toDomain(PhysicalInventoryDTO dto) {
          if (dto == null)
               return null;

          PhysicalInventory entity = new PhysicalInventory();

          entity.setId(dto.getId());
          entity.setMunicipalityId(dto.getMunicipalityId());
          entity.setInventoryNumber(dto.getInventoryNumber());
          entity.setInventoryType(dto.getInventoryType());
          entity.setDescription(dto.getDescription());
          entity.setAreaId(dto.getAreaId());
          entity.setCategoryId(dto.getCategoryId());
          entity.setLocationId(dto.getLocationId());
          entity.setPlannedStartDate(dto.getPlannedStartDate());
          entity.setPlannedEndDate(dto.getPlannedEndDate());
          entity.setGeneralResponsibleId(dto.getGeneralResponsibleId());
          entity.setIncludesMissing(dto.getIncludesMissing());
          entity.setIncludesSurplus(dto.getIncludesSurplus());
          entity.setRequiresPhotos(dto.getRequiresPhotos());
          entity.setObservations(dto.getObservations());
          entity.setInventoryTeam(dto.getInventoryTeam());
          entity.setAttachedDocuments(dto.getAttachedDocuments());

          return entity;
     }

     public static PhysicalInventoryDTO toDTO(PhysicalInventory entity) {
          if (entity == null)
               return null;

          PhysicalInventoryDTO dto = new PhysicalInventoryDTO();

          dto.setId(entity.getId());
          dto.setMunicipalityId(entity.getMunicipalityId());
          dto.setInventoryNumber(entity.getInventoryNumber());
          dto.setInventoryType(entity.getInventoryType());
          dto.setDescription(entity.getDescription());
          dto.setAreaId(entity.getAreaId());
          dto.setCategoryId(entity.getCategoryId());
          dto.setLocationId(entity.getLocationId());
          dto.setPlannedStartDate(entity.getPlannedStartDate());
          dto.setPlannedEndDate(entity.getPlannedEndDate());
          dto.setGeneralResponsibleId(entity.getGeneralResponsibleId());
          dto.setIncludesMissing(entity.getIncludesMissing());
          dto.setIncludesSurplus(entity.getIncludesSurplus());
          dto.setRequiresPhotos(entity.getRequiresPhotos());
          dto.setObservations(entity.getObservations());
          dto.setInventoryTeam(entity.getInventoryTeam());
          dto.setAttachedDocuments(entity.getAttachedDocuments());

          return dto;
     }
}