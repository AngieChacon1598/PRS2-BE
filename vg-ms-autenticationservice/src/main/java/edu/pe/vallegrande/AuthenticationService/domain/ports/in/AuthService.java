package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginFailureInfo;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginResult;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.RefreshTokenCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import reactor.core.publisher.Mono;

/**
 * Servicio de autenticación
 */
public interface AuthService {
    
    /**
     * Iniciar sesión
     */
    Mono<LoginResult> login(LoginCommand command);
    
    /**
     * Cerrar sesión invalidando token y cache
     */
    Mono<Void> logout(String token);

    Mono<Void> logout(String userId, String keycloakId, String municipalCode);
    
    /**
     * Renovar token
     */
    Mono<AuthTokens> refreshToken(RefreshTokenCommand command);
    
    /**
     * Validar token
     */
    Mono<Boolean> validateToken(String token);

    /**
     * Info adicional para respuestas de error de login (sin romper contrato web)
     */
    Mono<LoginFailureInfo> getLoginFailureInfo(String username);
}