package edu.pe.vallegrande.AuthenticationService.domain.model.user;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserAccount {
    UUID id;
    String username;
    String passwordHash;
    UUID personId;
    UUID areaId;
    UUID positionId;
    UUID directManagerId;
    UUID municipalCode;
    String status;
    LocalDateTime lastLogin;
    Integer loginAttempts;
    LocalDateTime blockedUntil;
    String blockReason;
    LocalDateTime blockStart;
    String suspensionReason;
    LocalDateTime suspensionStart;
    LocalDateTime suspensionEnd;
    UUID suspendedBy;
    String preferences;
    UUID createdBy;
    LocalDateTime createdAt;
    UUID updatedBy;
    LocalDateTime updatedAt;
    LocalDateTime passwordLastChanged;
    Boolean requiresPasswordReset;
    String keycloakId;
    Integer version;
}

