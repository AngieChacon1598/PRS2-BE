package edu.pe.vallegrande.AuthenticationService.domain.model.assignment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RolePermissionAssignment {
    UUID roleId;
    String roleName;
    UUID permissionId;
    String module;
    String action;
    String resource;
    String description;
    LocalDateTime createdAt;
    UUID municipalCode;
}

