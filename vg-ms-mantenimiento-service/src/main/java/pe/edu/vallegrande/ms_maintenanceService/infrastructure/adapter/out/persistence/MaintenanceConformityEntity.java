package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("maintenance_conformity")
public class MaintenanceConformityEntity {

    @Id
    private UUID id;

    @Column("maintenance_id")
    private UUID maintenanceId;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("conformity_number")
    private String conformityNumber;

    @Column("work_quality")
    private String workQuality;

    @Column("asset_condition_after")
    private String assetConditionAfter;

    @Column("observations")
    private String observations;

    @Column("confirmed_by")
    private UUID confirmedBy;

    @Column("confirmed_at")
    private LocalDateTime confirmedAt;

    @Column("digital_signature")
    private String digitalSignature;

    @Column("requires_followup")
    private Boolean requiresFollowup;

    @Column("followup_description")
    private String followupDescription;

    @Column("created_at")
    private LocalDateTime createdAt;
}
