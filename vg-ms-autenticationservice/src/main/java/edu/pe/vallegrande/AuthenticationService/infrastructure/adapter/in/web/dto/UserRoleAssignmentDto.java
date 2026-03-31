package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para asignaciones de roles a usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleAssignmentDto {

    private UUID userId;
    private String username;
    private UUID roleId;
    private String roleName;
    private String roleDescription;
    private UUID assignedBy;
    private String assignedByUsername;
    private LocalDateTime assignedAt;
    private LocalDate expirationDate;
    private Boolean active;
    private UUID municipalCode;
}