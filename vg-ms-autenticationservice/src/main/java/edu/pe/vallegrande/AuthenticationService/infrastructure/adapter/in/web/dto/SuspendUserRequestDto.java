package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitudes de suspensión de usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserRequestDto {
    
    @NotBlank(message = "El motivo de suspensión es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;
    
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime suspensionEnd;
}
