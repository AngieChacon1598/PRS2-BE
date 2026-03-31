package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las solicitudes de creación y actualización de permisos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDto {

    @NotBlank(message = "El módulo es obligatorio")
    @Size(min = 1, max = 50, message = "El módulo debe tener entre 1 y 50 caracteres")
    private String module;

    @NotBlank(message = "La acción es obligatoria")
    @Size(min = 2, max = 50, message = "La acción debe tener entre 2 y 50 caracteres")
    private String action;

    @Size(max = 100, message = "El recurso no puede exceder 100 caracteres")
    private String resource;

    @Size(max = 100, message = "El nombre amigable no puede exceder 100 caracteres")
    private String displayName;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
}