package edu.pe.vallegrande.AuthenticationService.domain.model.assignment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RolePermissionLink {
    UUID roleId;
    UUID permissionId;
    LocalDateTime createdAt;
    UUID municipalCode;
}

