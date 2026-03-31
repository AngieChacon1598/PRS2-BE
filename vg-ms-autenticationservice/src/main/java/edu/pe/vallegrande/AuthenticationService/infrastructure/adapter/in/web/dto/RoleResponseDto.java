package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para las respuestas de roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {
    
    private UUID id;
    private String name;
    private String description;
    private Boolean isSystem;
    private Boolean active;
    private LocalDateTime createdAt;
    private UUID createdBy;
    private UUID municipalCode;
}