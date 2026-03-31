package pe.edu.vallegrande.ms_maintenanceService.application.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import pe.edu.vallegrande.ms_maintenanceService.application.dto.MaintenanceRequestDTO;
import pe.edu.vallegrande.ms_maintenanceService.application.dto.MaintenanceResponseDTO;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;

public class MaintenanceMapper {

    public Maintenance toEntity(MaintenanceRequestDTO dto) {
        return Maintenance.builder()
                .municipalityId(dto.getMunicipalityId())
                .maintenanceCode(dto.getMaintenanceCode())
                .assetId(dto.getAssetId())
                .maintenanceType(dto.getMaintenanceType())
                .priority(dto.getPriority())
                .scheduledDate(dto.getScheduledDate())
                .workDescription(dto.getWorkDescription())
                .reportedProblem(dto.getReportedProblem())
                .observations(dto.getObservations())
                .technicalResponsibleId(dto.getTechnicalResponsibleId())
                .serviceSupplierId(dto.getServiceSupplierId())
                .supervisorId(dto.getSupervisorId())
                .laborCost(dto.getLaborCost() != null ? dto.getLaborCost() : BigDecimal.ZERO)
                .additionalCost(dto.getAdditionalCost() != null ? dto.getAdditionalCost() : BigDecimal.ZERO)
                .hasWarranty(dto.getHasWarranty())
                .warrantyExpirationDate(dto.getWarrantyExpirationDate())
                .requestedBy(dto.getRequestedBy())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public MaintenanceResponseDTO toResponseDTO(Maintenance entity) {
        return MaintenanceResponseDTO.builder()
                .id(entity.getId())
                .municipalityId(entity.getMunicipalityId())
                .maintenanceCode(entity.getMaintenanceCode())
                .assetId(entity.getAssetId())
                .maintenanceType(entity.getMaintenanceType())
                .priority(entity.getPriority())
                .scheduledDate(entity.getScheduledDate())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .nextDate(entity.getNextDate())
                .workDescription(entity.getWorkDescription())
                .reportedProblem(entity.getReportedProblem())
                .appliedSolution(entity.getAppliedSolution())
                .observations(entity.getObservations())
                .technicalResponsibleId(entity.getTechnicalResponsibleId())
                .serviceSupplierId(entity.getServiceSupplierId())
                .supervisorId(entity.getSupervisorId())
                .laborCost(entity.getLaborCost())
                .partsCost(entity.getPartsCost())
                .additionalCost(entity.getAdditionalCost())
                .totalCost(entity.getTotalCost())
                .maintenanceStatus(entity.getMaintenanceStatus())
                .workOrder(entity.getWorkOrder())
                .hasWarranty(entity.getHasWarranty())
                .warrantyExpirationDate(entity.getWarrantyExpirationDate())
                .requestedBy(entity.getRequestedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public Maintenance updateEntity(Maintenance existing, Maintenance updatedData) {
        if (updatedData.getMunicipalityId() != null) existing.setMunicipalityId(updatedData.getMunicipalityId());
        if (updatedData.getMaintenanceCode() != null) existing.setMaintenanceCode(updatedData.getMaintenanceCode());
        if (updatedData.getAssetId() != null) existing.setAssetId(updatedData.getAssetId());
        if (updatedData.getMaintenanceType() != null) existing.setMaintenanceType(updatedData.getMaintenanceType());
        if (updatedData.getPriority() != null) existing.setPriority(updatedData.getPriority());
        if (updatedData.getScheduledDate() != null) existing.setScheduledDate(updatedData.getScheduledDate());
        if (updatedData.getWorkDescription() != null) existing.setWorkDescription(updatedData.getWorkDescription());
        if (updatedData.getReportedProblem() != null) existing.setReportedProblem(updatedData.getReportedProblem());
        if (updatedData.getTechnicalResponsibleId() != null) existing.setTechnicalResponsibleId(updatedData.getTechnicalResponsibleId());
        if (updatedData.getServiceSupplierId() != null) existing.setServiceSupplierId(updatedData.getServiceSupplierId());
        if (updatedData.getSupervisorId() != null) existing.setSupervisorId(updatedData.getSupervisorId());
        if (updatedData.getHasWarranty() != null) {
            existing.setHasWarranty(updatedData.getHasWarranty());
            if (Boolean.FALSE.equals(updatedData.getHasWarranty())) {
                existing.setWarrantyExpirationDate(null);
            }
        }
        if (updatedData.getWarrantyExpirationDate() != null) {
            existing.setWarrantyExpirationDate(updatedData.getWarrantyExpirationDate());
        }
        if (updatedData.getLaborCost() != null) existing.setLaborCost(updatedData.getLaborCost());
        if (updatedData.getAdditionalCost() != null) existing.setAdditionalCost(updatedData.getAdditionalCost());
        
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }
}
