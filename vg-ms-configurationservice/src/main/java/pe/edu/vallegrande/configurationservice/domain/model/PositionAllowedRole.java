package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("position_allowed_roles")
public class PositionAllowedRole {

    @Id
    private UUID id;

    @Column("position_id")
    private UUID positionId;

    @Column("area_id")
    private UUID areaId; // NULL = aplica a cualquier área

    @Column("role_id")
    private UUID roleId;

    @Column("is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
