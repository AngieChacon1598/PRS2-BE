package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para las respuestas de usuarios
 * No incluye información sensible como password_hash
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private UUID id;
    private String username;
    private UUID personId;
    private UUID areaId;
    private UUID positionId;
    private UUID directManagerId;
    private UUID municipalCode;
    private String status;
    private LocalDateTime lastLogin;
    private Integer loginAttempts;
    
    // Información de bloqueo
    private LocalDateTime blockedUntil;
    private String blockReason;
    private LocalDateTime blockStart;
    
    // Información de suspensión
    private String suspensionReason;
    private LocalDateTime suspensionStart;
    private LocalDateTime suspensionEnd;
    private UUID suspendedBy;
    
    private Map<String, Object> preferences;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime passwordLastChanged;
    private Boolean requiresPasswordReset;
    private Integer version;
}