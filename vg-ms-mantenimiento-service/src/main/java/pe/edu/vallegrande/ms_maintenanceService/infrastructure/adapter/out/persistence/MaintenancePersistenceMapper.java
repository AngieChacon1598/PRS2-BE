package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenancePart;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceStatusHistory;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.MaintenanceConformity;

@Component
@RequiredArgsConstructor
public class MaintenancePersistenceMapper {

    public Maintenance toDomain(MaintenanceEntity entity) {
        if (entity == null) return null;
        return Maintenance.builder()
                .id(entity.getId())
                .municipalityId(entity.getMunicipalityId())
                .maintenanceCode(entity.getMaintenanceCode())
                .assetId(entity.getAssetId())
                .assetCode(entity.getAssetCode())
                .assetDescription(entity.getAssetDescription())
                .maintenanceType(entity.getMaintenanceType())
                .isScheduled(entity.getIsScheduled())
                .priority(entity.getPriority())
                .scheduledDate(entity.getScheduledDate())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .nextDate(entity.getNextDate())
                .estimatedDurationHours(entity.getEstimatedDurationHours())
                .workDescription(entity.getWorkDescription())
                .reportedProblem(entity.getReportedProblem())
                .appliedSolution(entity.getAppliedSolution())
                .observations(entity.getObservations())
                .technicalResponsibleId(entity.getTechnicalResponsibleId())
                .supervisorId(entity.getSupervisorId())
                .requestedBy(entity.getRequestedBy())
                .serviceSupplierId(entity.getServiceSupplierId())
                .laborCost(entity.getLaborCost())
                .partsCost(entity.getPartsCost())
                .additionalCost(entity.getAdditionalCost())
                .totalCost(entity.getTotalCost())
                .maintenanceStatus(entity.getMaintenanceStatus())
                .workOrder(entity.getWorkOrder())
                .purchaseOrder(entity.getPurchaseOrder())
                .invoiceNumber(entity.getInvoiceNumber())
                .hasWarranty(entity.getHasWarranty())
                .warrantyExpirationDate(entity.getWarrantyExpirationDate())
                .warrantyDescription(entity.getWarrantyDescription())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MaintenanceEntity toEntity(Maintenance domain) {
        if (domain == null) return null;
        return MaintenanceEntity.builder()
                .id(domain.getId())
                .municipalityId(domain.getMunicipalityId())
                .maintenanceCode(domain.getMaintenanceCode())
                .assetId(domain.getAssetId())
                .assetCode(domain.getAssetCode())
                .assetDescription(domain.getAssetDescription())
                .maintenanceType(domain.getMaintenanceType())
                .isScheduled(domain.getIsScheduled())
                .priority(domain.getPriority())
                .scheduledDate(domain.getScheduledDate())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .nextDate(domain.getNextDate())
                .estimatedDurationHours(domain.getEstimatedDurationHours())
                .workDescription(domain.getWorkDescription())
                .reportedProblem(domain.getReportedProblem())
                .appliedSolution(domain.getAppliedSolution())
                .observations(domain.getObservations())
                .technicalResponsibleId(domain.getTechnicalResponsibleId())
                .supervisorId(domain.getSupervisorId())
                .requestedBy(domain.getRequestedBy())
                .serviceSupplierId(domain.getServiceSupplierId())
                .laborCost(domain.getLaborCost())
                .partsCost(domain.getPartsCost())
                .additionalCost(domain.getAdditionalCost())
                .totalCost(domain.getTotalCost())
                .maintenanceStatus(domain.getMaintenanceStatus())
                .workOrder(domain.getWorkOrder())
                .purchaseOrder(domain.getPurchaseOrder())
                .invoiceNumber(domain.getInvoiceNumber())
                .hasWarranty(domain.getHasWarranty())
                .warrantyExpirationDate(domain.getWarrantyExpirationDate())
                .warrantyDescription(domain.getWarrantyDescription())
                .createdAt(domain.getCreatedAt())
                .updatedBy(domain.getUpdatedBy())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    // Mappers para las nuevas entidades de detalle
    
    public MaintenancePart toDomain(MaintenancePartEntity entity) {
        if (entity == null) return null;
        return MaintenancePart.builder()
                .id(entity.getId())
                .maintenanceId(entity.getMaintenanceId())
                .municipalityId(entity.getMunicipalityId())
                .partCode(entity.getPartCode())
                .partName(entity.getPartName())
                .description(entity.getDescription())
                .partType(entity.getPartType())
                .quantity(entity.getQuantity())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .unitPrice(entity.getUnitPrice())
                .subtotal(entity.getSubtotal())
                .supplierId(entity.getSupplierId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public MaintenancePartEntity toEntity(MaintenancePart domain) {
        if (domain == null) return null;
        return MaintenancePartEntity.builder()
                .id(domain.getId())
                .maintenanceId(domain.getMaintenanceId())
                .municipalityId(domain.getMunicipalityId())
                .partCode(domain.getPartCode())
                .partName(domain.getPartName())
                .description(domain.getDescription())
                .partType(domain.getPartType())
                .quantity(domain.getQuantity())
                .unitOfMeasure(domain.getUnitOfMeasure())
                .unitPrice(domain.getUnitPrice())
                .subtotal(domain.getSubtotal())
                .supplierId(domain.getSupplierId())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public MaintenanceStatusHistory toDomain(MaintenanceStatusHistoryEntity entity) {
        if (entity == null) return null;
        return MaintenanceStatusHistory.builder()
                .id(entity.getId())
                .maintenanceId(entity.getMaintenanceId())
                .municipalityId(entity.getMunicipalityId())
                .previousStatus(entity.getPreviousStatus())
                .newStatus(entity.getNewStatus())
                .reason(entity.getReason())
                .changedBy(entity.getChangedBy())
                .changedAt(entity.getChangedAt())
                .build();
    }

    public MaintenanceStatusHistoryEntity toEntity(MaintenanceStatusHistory domain) {
        if (domain == null) return null;
        return MaintenanceStatusHistoryEntity.builder()
                .id(domain.getId())
                .maintenanceId(domain.getMaintenanceId())
                .municipalityId(domain.getMunicipalityId())
                .previousStatus(domain.getPreviousStatus())
                .newStatus(domain.getNewStatus())
                .reason(domain.getReason())
                .changedBy(domain.getChangedBy())
                .changedAt(domain.getChangedAt())
                .build();
    }

    public MaintenanceConformity toDomain(MaintenanceConformityEntity entity) {
        if (entity == null) return null;
        return MaintenanceConformity.builder()
                .id(entity.getId())
                .maintenanceId(entity.getMaintenanceId())
                .municipalityId(entity.getMunicipalityId())
                .conformityNumber(entity.getConformityNumber())
                .workQuality(entity.getWorkQuality())
                .assetConditionAfter(entity.getAssetConditionAfter())
                .observations(entity.getObservations())
                .confirmedBy(entity.getConfirmedBy())
                .confirmedAt(entity.getConfirmedAt())
                .digitalSignature(entity.getDigitalSignature())
                .requiresFollowup(entity.getRequiresFollowup())
                .followupDescription(entity.getFollowupDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public MaintenanceConformityEntity toEntity(MaintenanceConformity domain) {
        if (domain == null) return null;
        return MaintenanceConformityEntity.builder()
                .id(domain.getId())
                .maintenanceId(domain.getMaintenanceId())
                .municipalityId(domain.getMunicipalityId())
                .conformityNumber(domain.getConformityNumber())
                .workQuality(domain.getWorkQuality())
                .assetConditionAfter(domain.getAssetConditionAfter())
                .observations(domain.getObservations())
                .confirmedBy(domain.getConfirmedBy())
                .confirmedAt(domain.getConfirmedAt())
                .digitalSignature(domain.getDigitalSignature())
                .requiresFollowup(domain.getRequiresFollowup())
                .followupDescription(domain.getFollowupDescription())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
