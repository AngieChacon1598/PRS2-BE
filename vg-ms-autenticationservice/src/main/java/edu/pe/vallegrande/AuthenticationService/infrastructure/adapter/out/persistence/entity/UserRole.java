package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad UserRole para la relación usuarios-roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users_roles")
public class UserRole {

    @Column("user_id")
    private UUID userId;

    @Column("role_id")
    private UUID roleId;

    @Column("assigned_by")
    private UUID assignedBy;

    @Column("assigned_at")
    private LocalDateTime assignedAt;

    @Column("expiration_date")
    private LocalDate expirationDate;

    @Column("active")
    private Boolean active;

    @Column("municipal_code")
    private UUID municipalCode;
}