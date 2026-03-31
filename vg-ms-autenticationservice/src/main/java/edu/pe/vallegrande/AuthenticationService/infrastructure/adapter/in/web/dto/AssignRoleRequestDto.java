package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para solicitudes de asignación de roles
 * assignedBy y municipalCode se obtienen automáticamente del usuario autenticado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequestDto {
    
    private LocalDate expirationDate;
    private Boolean active;
}