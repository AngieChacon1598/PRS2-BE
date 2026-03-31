package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las solicitudes de creación y actualización de roles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {
    
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del rol debe tener entre 3 y 50 caracteres")
    private String name;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
    
    private Boolean isSystem;
    private Boolean active;
}