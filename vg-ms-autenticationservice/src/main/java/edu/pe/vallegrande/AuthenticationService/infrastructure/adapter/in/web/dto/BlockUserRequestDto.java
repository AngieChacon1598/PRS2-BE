package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para solicitudes de bloqueo de usuarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequestDto {
    
    @NotBlank(message = "El motivo de bloqueo es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String reason;
    
    @Future(message = "La fecha de fin debe ser futura")
    private LocalDateTime blockedUntil;
    
    @Min(value = 1, message = "La duración debe ser al menos 1 hora")
    private Integer durationHours;
}
