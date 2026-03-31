package pe.edu.vallegrande.movementservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("username")
    private String username;

    @Column("password_hash")
    private String passwordHash;

    @Column("person_id")
    private UUID personId;

    @Column("area_id")
    private UUID areaId;

    @Column("position_id")
    private UUID positionId;

    @Column("direct_supervisor_id")
    private UUID directSupervisorId;

    @Column("status")
    private String status;

    @Column("last_access")
    private LocalDateTime lastAccess;

    @Column("login_attempts")
    private Integer loginAttempts;

    @Column("locked_until")
    private LocalDateTime lockedUntil;

    @Column("preferences")
    private String preferences;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private UUID updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
