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
@Table("maintenance_status_history")
public class MaintenanceStatusHistoryEntity {

    @Id
    private UUID id;

    @Column("maintenance_id")
    private UUID maintenanceId;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("previous_status")
    private String previousStatus;

    @Column("new_status")
    private String newStatus;

    @Column("reason")
    private String reason;

    @Column("changed_by")
    private UUID changedBy;

    @Column("changed_at")
    private LocalDateTime changedAt;
}
