package edu.pe.vallegrande.AuthenticationService.domain.model.role;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RoleModel {
    UUID id;
    String name;
    String description;
    Boolean isSystem;
    Boolean active;
    LocalDateTime createdAt;
    UUID createdBy;
    UUID municipalCode;
}

