package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.LoginRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RefreshTokenRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.TokenResponseDto;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.AuthService;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper.AuthWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/** Controlador REST para autenticación */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API para autenticación y manejo de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas, usuario bloqueado o suspendido")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("Solicitud de login para usuario: {}", loginRequest.getUsername());

        return authService.login(AuthWebMapper.toCommand(loginRequest))
                .<ResponseEntity<?>>map(response -> ResponseEntity.ok(AuthWebMapper.toDto(response)))
                .onErrorResume(error -> {
                    log.error("Error en login: {}", error.getMessage());
                    
                    return authService.getLoginFailureInfo(loginRequest.getUsername())
                            .map(info -> {
                                Map<String, Object> errorResponse = new HashMap<>();
                                errorResponse.put("error", error.getMessage());

                                if (info.getBlockedUntil() == null) {
                                    if (info.getLoginAttempts() != null) {
                                        errorResponse.put("loginAttempts", info.getLoginAttempts());
                                    }
                                    if (info.getRemainingAttempts() != null) {
                                        errorResponse.put("remainingAttempts", info.getRemainingAttempts());
                                    }
                                } else {
                                    errorResponse.put("blockedUntil", info.getBlockedUntil().toString());
                                    errorResponse.put("blockReason", info.getBlockReason());
                                }

                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                            })
                            .switchIfEmpty(Mono.just(ResponseEntity
                                    .status(HttpStatus.UNAUTHORIZED)
                                    .body(Map.of("error", error.getMessage()))));
                });
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el token JWT actual y el cache de permisos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout exitoso"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Void>> logout(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token JWT en el header Authorization") @RequestHeader("Authorization") String authHeader) {

        log.info("Solicitud de logout completo");

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        String userId = jwt.getClaimAsString("user_id");
        String keycloakId = jwt.getSubject();
        String municipalCode = jwt.getClaimAsString("municipal_code");

        return authService.logout(token)
                .then(authService.logout(userId, keycloakId, municipalCode))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Renovar token", description = "Genera un nuevo access token usando el refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponseDto>> refreshToken(@RequestBody RefreshTokenRequestDto refreshRequest) {
        log.info("Solicitud de refresh token");

        return authService.refreshToken(AuthWebMapper.toCommand(refreshRequest))
                .map(response -> ResponseEntity.ok(AuthWebMapper.toDto(response)))
                .onErrorResume(error -> {
                    log.error("Error en refresh token: {}", error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(TokenResponseDto.builder().build()));
                });
    }

    @Operation(summary = "Validar token", description = "Verifica si un token JWT es válido")
    @ApiResponse(responseCode = "200", description = "Validación completada")
    @PostMapping("/validate")
    public Mono<ResponseEntity<Boolean>> validateToken(
            @Parameter(description = "Token JWT en el header Authorization") @RequestHeader("Authorization") String authHeader) {

        log.info("Solicitud de validación de token");

        // Extraer token del header "Bearer token"
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        return authService.validateToken(token)
                .map(isValid -> ResponseEntity.ok(isValid))
                .onErrorReturn(ResponseEntity.ok(false));
    }
}