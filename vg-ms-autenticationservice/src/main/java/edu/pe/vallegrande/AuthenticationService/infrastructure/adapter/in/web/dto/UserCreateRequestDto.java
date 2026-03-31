package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las solicitudes de creación de usuarios
 * Requiere password obligatorio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

    @NotBlank(message = "El username es obligatorio")
    @Size(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, message = "El username debe tener entre "
            + MIN_USERNAME_LENGTH + " y " + MAX_USERNAME_LENGTH + " caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH, message = "La contraseña debe tener entre "
            + MIN_PASSWORD_LENGTH + " y " + MAX_PASSWORD_LENGTH + " caracteres")
    @Pattern(regexp = PASSWORD_PATTERN, message = "La contraseña debe tener al menos una mayúscula, una minúscula y un número")
    private String password;

    private UUID personId;

    // Campos opcionales hasta que los microservicios de areas y positions estén
    // disponibles
    private UUID areaId;

    private UUID positionId;

    private UUID directManagerId;

    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED)?$", message = "El status debe ser ACTIVE, INACTIVE o SUSPENDED")
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    private Boolean requiresPasswordReset;

    private Map<String, Object> preferences;
}