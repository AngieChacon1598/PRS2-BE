package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {

    private UUID id;
    private String module;
    private String action;
    private String resource;
    private String displayName;
    private String description;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private Boolean status;
    private UUID municipalCode;
}