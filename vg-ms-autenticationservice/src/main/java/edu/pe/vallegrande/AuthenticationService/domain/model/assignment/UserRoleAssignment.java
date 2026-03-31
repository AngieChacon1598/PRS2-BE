package edu.pe.vallegrande.AuthenticationService.domain.model.assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserRoleAssignment {
    UUID userId;
    String username;
    UUID roleId;
    String roleName;
    String roleDescription;
    UUID assignedBy;
    String assignedByUsername;
    LocalDateTime assignedAt;
    LocalDate expirationDate;
    Boolean active;
    UUID municipalCode;
}

