package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el registro inicial de un nuevo Tenant (Municipalidad)
 * Utilizado por el microservicio de Municipalidades.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantOnboardingRequestDto {

    @NotBlank(message = "El username del administrador es obligatorio")
    private String adminUsername;

    @NotBlank(message = "La contraseña es obligatoria")
    private String adminPassword;

    @NotNull(message = "El código municipal es obligatorio")
    private UUID municipalCode;

    @NotBlank(message = "El nombre de la autoridad o municipalidad es obligatorio")
    private String authorityName;

    private String email;
}
