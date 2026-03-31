package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para asignaciones de permisos a roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionAssignmentDto {

    private UUID roleId;
    private String roleName;
    private UUID permissionId;
    private String module;
    private String action;
    private String resource;
    private String description;
    private LocalDateTime createdAt;
    private UUID municipalCode;
}