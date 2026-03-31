package edu.pe.vallegrande.AuthenticationService.domain.model.permission;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PermissionModel {
    UUID id;
    String module;
    String action;
    String resource;
    String displayName;
    String description;
    LocalDateTime createdAt;
    UUID createdBy;
    Boolean status;
    UUID municipalCode;
}

