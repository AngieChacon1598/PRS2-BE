package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad RolePermission para la relación roles-permisos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("roles_permissions")
public class RolePermission {

    @Column("role_id")
    private UUID roleId;

    @Column("permission_id")
    private UUID permissionId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("municipal_code")
    private UUID municipalCode;
}