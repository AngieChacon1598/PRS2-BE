package pe.edu.vallegrande.ms_maintenanceService.application.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;

class MaintenanceMapperTest {

    private final MaintenanceMapper mapper = new MaintenanceMapper();

    @Test
    void actualizarEntidadPreservaValoresOpcionalesCuandoLaSolicitudLosOmite() {
        LocalDateTime originalStartDate = LocalDateTime.of(2026, 3, 10, 8, 0);
        LocalDateTime originalEndDate = LocalDateTime.of(2026, 3, 10, 10, 0);
        LocalDate originalNextDate = LocalDate.of(2026, 3, 20);
        LocalDate originalWarrantyDate = LocalDate.of(2026, 12, 31);

        Maintenance existing = Maintenance.builder()
                .municipalityId(UUID.randomUUID())
                .maintenanceCode("MANT-001")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(LocalDate.of(2026, 3, 12))
                .workDescription("Trabajo actual")
                .reportedProblem("Problema actual")
                .technicalResponsibleId(UUID.randomUUID())
                .serviceSupplierId(UUID.randomUUID())
                .hasWarranty(true)
                .startDate(originalStartDate)
                .endDate(originalEndDate)
                .nextDate(originalNextDate)
                .appliedSolution("Solución original")
                .observations("Observación original")
                .laborCost(new BigDecimal("125.50"))
                .partsCost(new BigDecimal("25.00"))
                .totalCost(new BigDecimal("150.50"))
                .maintenanceStatus("IN_PROCESS")
                .workOrder("WO-ORIGINAL")
                .warrantyExpirationDate(originalWarrantyDate)
                .build();

        Maintenance updatedData = Maintenance.builder()
                .municipalityId(existing.getMunicipalityId())
                .maintenanceCode("MANT-001-A")
                .assetId(existing.getAssetId())
                .maintenanceType("CORRECTIVE")
                .priority("CRITICAL")
                .scheduledDate(LocalDate.of(2026, 3, 14))
                .workDescription("Trabajo actualizado")
                .reportedProblem("Problema actualizado")
                .technicalResponsibleId(existing.getTechnicalResponsibleId())
                .serviceSupplierId(existing.getServiceSupplierId())
                .hasWarranty(true)
                .updatedBy(UUID.randomUUID())
                .build();

        Maintenance result = mapper.updateEntity(existing, updatedData);

        assertEquals("MANT-001-A", result.getMaintenanceCode());
        assertEquals("CORRECTIVE", result.getMaintenanceType());
        assertEquals("CRITICAL", result.getPriority());
        assertEquals(originalStartDate, result.getStartDate());
        assertEquals(originalEndDate, result.getEndDate());
        assertEquals(originalNextDate, result.getNextDate());
        assertEquals("Solución original", result.getAppliedSolution());
        assertEquals("Observación original", result.getObservations());
        assertEquals(new BigDecimal("125.50"), result.getLaborCost());
        assertEquals(new BigDecimal("25.00"), result.getPartsCost());
        assertEquals(new BigDecimal("150.50"), result.getTotalCost());
        assertEquals("IN_PROCESS", result.getMaintenanceStatus());
        assertEquals("WO-ORIGINAL", result.getWorkOrder());
        assertEquals(originalWarrantyDate, result.getWarrantyExpirationDate());
    }

    @Test
    void actualizarEntidadLimpiaFechaDeGarantiaCuandoLaGarantiaEstaDeshabilitada() {
        Maintenance existing = Maintenance.builder()
                .municipalityId(UUID.randomUUID())
                .maintenanceCode("MANT-002")
                .assetId(UUID.randomUUID())
                .maintenanceType("PREVENTIVE")
                .priority("HIGH")
                .scheduledDate(LocalDate.of(2026, 3, 12))
                .workDescription("Trabajo")
                .reportedProblem("Problema")
                .technicalResponsibleId(UUID.randomUUID())
                .serviceSupplierId(UUID.randomUUID())
                .hasWarranty(true)
                .warrantyExpirationDate(LocalDate.of(2026, 12, 31))
                .build();

        Maintenance updatedData = Maintenance.builder()
                .municipalityId(existing.getMunicipalityId())
                .maintenanceCode(existing.getMaintenanceCode())
                .assetId(existing.getAssetId())
                .maintenanceType(existing.getMaintenanceType())
                .priority(existing.getPriority())
                .scheduledDate(existing.getScheduledDate())
                .workDescription(existing.getWorkDescription())
                .reportedProblem(existing.getReportedProblem())
                .technicalResponsibleId(existing.getTechnicalResponsibleId())
                .serviceSupplierId(existing.getServiceSupplierId())
                .hasWarranty(false)
                .updatedBy(UUID.randomUUID())
                .build();

        Maintenance result = mapper.updateEntity(existing, updatedData);

        assertEquals(false, result.getHasWarranty());
        assertNull(result.getWarrantyExpirationDate());
    }
}