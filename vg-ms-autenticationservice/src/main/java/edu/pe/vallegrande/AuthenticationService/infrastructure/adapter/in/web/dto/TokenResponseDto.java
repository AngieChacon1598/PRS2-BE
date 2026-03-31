package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuestas de tokens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDto {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // segundos
}