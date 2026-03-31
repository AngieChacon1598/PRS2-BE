package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginFailureInfo;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginResult;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.RefreshTokenCommand;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.AuthService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;

import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AuthUserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.TokenBlacklistPort;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.application.util.DateTimeUtil;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ExternalAuthPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/** Implementación del servicio de autenticación */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserPort authUserPort;

    private final JwtService jwtService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final ExternalAuthPort externalAuthPort;
    private final CachePort cachePort;

    @org.springframework.beans.factory.annotation.Value("${keycloak.realm}")
    private String defaultRealm;


    @Override
    public Mono<LoginResult> login(LoginCommand command) {
        log.info("Intento de login para usuario: {}", command.getUsername());

        return authUserPort.findByUsername(command.getUsername())
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Validar password
                    if (!validatePassword(command.getPassword(), user.getPasswordHash())) {
                        return incrementLoginAttempts(user)
                                .then(Mono.error(new RuntimeException("Credenciales inválidas")));
                    }

                    // Verificar si está bloqueado
                    if (user.getBlockedUntil() != null) {
                        LocalDateTime now = DateTimeUtil.nowInPeru();
                        log.info(
                                "Verificando estado de bloqueo - Usuario: {}, BlockedUntil: {}, Hora actual: {}, Diferencia: {} minutos",
                                user.getUsername(),
                                user.getBlockedUntil(),
                                now,
                                DateTimeUtil.minutesBetween(now, user.getBlockedUntil()));

                        if (user.getBlockedUntil().isAfter(now)) {
                            // Aún está bloqueado
                            String blockMessage = "Usuario bloqueado hasta: "
                                    + DateTimeUtil.formatForDisplay(user.getBlockedUntil());
                            if (user.getBlockReason() != null) {
                                blockMessage += ". Motivo: " + user.getBlockReason();
                            }
                            log.warn("Usuario {} aún bloqueado. Expira en {} minutos",
                                    user.getUsername(),
                                    DateTimeUtil.minutesBetween(now, user.getBlockedUntil()));
                            return Mono.error(new RuntimeException(blockMessage));
                        } else {
                            // El bloqueo ya expiró, desbloquearlo automáticamente
                            log.info("Desbloqueando automáticamente usuario: {} (bloqueo expirado hace {} minutos)",
                                    user.getUsername(),
                                    DateTimeUtil.minutesBetween(user.getBlockedUntil(), now));
                            return authUserPort.unblockUser(user.getId())
                                    .then(authUserPort.findById(user.getId()))
                                    .flatMap(updatedUser -> validateUserStatusAndLogin(updatedUser, command.getPassword()));
                        }
                    }

                    // Continuar con validaciones normales (pasando el password para Keycloak)
                    return validateUserStatusAndLogin(user, command.getPassword());
                })
                .doOnSuccess(response -> log.info("Login exitoso para usuario: {}", command.getUsername()))
                .doOnError(error -> log.error("Error en login para usuario {}: {}", command.getUsername(),
                        error.getMessage()));
    }

    /** Valida estado del usuario y procesa login */
    private Mono<LoginResult> validateUserStatusAndLogin(UserAccount user, String password) {
        // Verificar si está suspendido
        if ("SUSPENDED".equals(user.getStatus())) {
            String suspensionMessage = "Usuario suspendido";
            if (user.getSuspensionEnd() != null) {
                suspensionMessage += " hasta: " + user.getSuspensionEnd();
            }
            if (user.getSuspensionReason() != null) {
                suspensionMessage += ". Motivo: " + user.getSuspensionReason();
            }
            return Mono.error(new RuntimeException(suspensionMessage));
        }

        // Validar estado del usuario
        if (!"ACTIVE".equals(user.getStatus())) {
            return Mono.error(new RuntimeException("Usuario inactivo"));
        }

        // Login exitoso delegando a Keycloak
        return processSuccessfulLogin(user, password);
    }

    @Override
    public Mono<Void> logout(String token) {
        log.info("Cerrando sesión (token blacklist)");

        return tokenBlacklistPort.blacklist(token)
                .doOnSuccess(unused -> log.info("Sesión cerrada exitosamente"));
    }

    @Override
    public Mono<Void> logout(String userId, String keycloakId, String municipalCode) {
        log.info("Cerrando sesión para usuario: {}, municipalidad: {}", userId, municipalCode);
        return cachePort.invalidate(userId, municipalCode)
                .doOnSuccess(unused -> log.info("Cache de permisos invalidado para usuario: {}", userId));
    }

    @Override
    public Mono<AuthTokens> refreshToken(RefreshTokenCommand command) {
        log.info("Renovando token a través de Keycloak");

        String realm = defaultRealm;

        return externalAuthPort.refreshToken(command.getRefreshToken(), realm)
                .doOnSuccess(response -> log.info("Token renovado exitosamente en Keycloak"))
                .onErrorResume(e -> {
                    log.error("Error al renovar token en Keycloak: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error al renovar sesión: " + e.getMessage()));
                });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return tokenBlacklistPort.isBlacklisted(token)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return Mono.just(false);
                    }
                    return jwtService.validateToken(token);
                });
    }

    @Override
    public Mono<LoginFailureInfo> getLoginFailureInfo(String username) {
        return authUserPort.findByUsername(username)
                .map(user -> {
                    if (user.getBlockedUntil() == null || DateTimeUtil.isPast(user.getBlockedUntil())) {
                        int attempts = user.getLoginAttempts() != null ? user.getLoginAttempts() : 0;
                        int remaining = 3 - attempts;
                        return LoginFailureInfo.builder()
                                .loginAttempts(attempts)
                                .remainingAttempts(Math.max(0, remaining))
                                .build();
                    }
                    return LoginFailureInfo.builder()
                            .blockedUntil(user.getBlockedUntil())
                            .blockReason(user.getBlockReason())
                            .build();
                });
    }

    /**
     * Procesar login exitoso delegando a Keycloak
     */
    private Mono<LoginResult> processSuccessfulLogin(UserAccount user, String password) {
        // Usamos el realm configurado por defecto para evitar el 404 con UUIDs dinámicos
        String realm = defaultRealm;

        log.info("Delegando autenticación a Keycloak - Usuario: {}, Realm: {}", user.getUsername(), realm);

        // Verificar si necesita reset por antigüedad (90 días)
        boolean isExpired = false;
        if (user.getPasswordLastChanged() != null) {
            LocalDateTime expiryDate = user.getPasswordLastChanged().plusDays(90);
            isExpired = DateTimeUtil.nowInPeru().isAfter(expiryDate);
        }

        boolean resetRequired = (user.getRequiresPasswordReset() != null && user.getRequiresPasswordReset()) || isExpired;

        // 1. Actualizar último login en nuestra BD
        return authUserPort.updateLastLogin(user.getId(), DateTimeUtil.nowInPeru())
                .then(Mono.zip(
                        // 2. Obtener roles actuales
                        authUserPort.findActiveRoleNames(user.getId()).collect(Collectors.toList()),
                        // 3. Pedir token a Keycloak (ROPC)
                        externalAuthPort.login(user.getUsername(), password, realm)
                ))
                .flatMap(tuple -> {
                    List<String> roleNames = tuple.getT1();
                    AuthTokens keycloakTokens = tuple.getT2();

                    return Mono.just(LoginResult.builder()
                            .tokens(keycloakTokens)
                            .userId(user.getId())
                            .username(user.getUsername())
                            .municipalCode(user.getMunicipalCode())
                            .status(user.getStatus())
                            .roles(roleNames)
                            .loginTime(DateTimeUtil.nowInPeru())
                            .requiresPasswordReset(resetRequired)
                            .build());
                })
                .onErrorResume(e -> {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "Error desconocido";
                    log.error("Error al autenticar con Keycloak para {}: {}", user.getUsername(), errorMsg);

                    // Si es un error de credenciales de Keycloak, pasar el mensaje directamente
                    if (errorMsg.contains("Credenciales inválidas") || errorMsg.contains("invalid_grant")) {
                        return Mono.error(new RuntimeException("Credenciales inválidas"));
                    }

                    // Si es un error de disponibilidad/timeout, mensaje amigable
                    if (errorMsg.contains("no disponible") || errorMsg.contains("no respondió") || errorMsg.contains("no se pudo conectar")) {
                        return Mono.error(new RuntimeException(errorMsg));
                    }

                    // Error genérico con más contexto
                    return Mono.error(new RuntimeException("Error de comunicación con el servicio de identidad. Por favor, inténtelo nuevamente."));
                });
    }

    /** Incrementa intentos de login fallidos y bloquea si supera el límite */
    private Mono<Void> incrementLoginAttempts(UserAccount user) {
        return authUserPort.incrementLoginAttempts(user.getId())
                .then(authUserPort.findById(user.getId()))
                .flatMap(updatedUser -> {
                    // Si supera 3 intentos, bloquear por 30 minutos
                    if (updatedUser.getLoginAttempts() != null && updatedUser.getLoginAttempts() >= 3) {
                        LocalDateTime now = DateTimeUtil.nowInPeru();
                        LocalDateTime blockedUntil = DateTimeUtil.addMinutes(now, 30);
                        String blockReason = "Cuenta bloqueada por 3 intentos fallidos de inicio de sesión";

                        log.warn("Usuario {} bloqueado por {} intentos fallidos de inicio de sesión",
                                updatedUser.getUsername(), updatedUser.getLoginAttempts());
                        log.warn("Hora actual (Perú): {}", DateTimeUtil.formatForDisplay(now));
                        log.warn("Bloqueado hasta: {}", DateTimeUtil.formatForDisplay(blockedUntil));
                        log.warn("Duración del bloqueo: 30 minutos");

                        UserAccount toSave = updatedUser.toBuilder()
                                .blockedUntil(blockedUntil)
                                .blockReason(blockReason)
                                .blockStart(now)
                                .updatedAt(now)
                                .build();

                        return authUserPort.save(toSave).then();
                    }
                    return Mono.empty();
                })
                .then();
    }

    /** Valida password usando BCrypt */
    private boolean validatePassword(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}