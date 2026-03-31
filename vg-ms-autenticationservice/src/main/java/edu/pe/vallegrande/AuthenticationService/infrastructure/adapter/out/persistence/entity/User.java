package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad User para el sistema de autenticación
 * Representa los usuarios del sistema con sus credenciales y datos básicos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    @Column("id")
    private UUID id;

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

    @Column("direct_manager_id")
    private UUID directManagerId;

    @Column("municipal_code")
    private UUID municipalCode;

    @Column("status")
    private String status;

    @Column("last_login")
    private LocalDateTime lastLogin;

    @Column("login_attempts")
    private Integer loginAttempts;

    @Column("blocked_until")
    private LocalDateTime blockedUntil;

    @Column("block_reason")
    private String blockReason;

    @Column("block_start")
    private LocalDateTime blockStart;

    @Column("suspension_reason")
    private String suspensionReason;

    @Column("suspension_start")
    private LocalDateTime suspensionStart;

    @Column("suspension_end")
    private LocalDateTime suspensionEnd;

    @Column("suspended_by")
    private UUID suspendedBy;

    @Column("preferences")
    private String preferences;

    @Column("created_by")
    @CreatedBy
    private UUID createdBy;

    @Column("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_by")
    @LastModifiedBy
    private UUID updatedBy;

    @Column("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("password_last_changed")
    private LocalDateTime passwordLastChanged;

    @Column("requires_password_reset")
    private Boolean requiresPasswordReset;

    @Column("keycloak_id")
    private String keycloakId;

    @org.springframework.data.annotation.Version
    @Column("version")
    private Integer version;
}