package edu.pe.vallegrande.AuthenticationService.domain.model.assignment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserRoleLink {
    UUID userId;
    UUID roleId;
    UUID assignedBy;
    LocalDateTime assignedAt;
    LocalDate expirationDate;
    Boolean active;
    UUID municipalCode;
}

