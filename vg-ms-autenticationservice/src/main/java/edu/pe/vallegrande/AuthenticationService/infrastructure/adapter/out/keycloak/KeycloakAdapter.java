package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.keycloak;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ExternalAuthPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.keycloak.dto.KeycloakTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class KeycloakAdapter implements ExternalAuthPort {

    private final WebClient webClient;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-realm:master}")
    private String adminRealm;

    @Value("${keycloak.admin-client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    // Cache thread-safe para el admin token
    private final AtomicReference<String> cachedAdminToken = new AtomicReference<>();
    private volatile long tokenExpiryTime = 0;

    // Timeouts y reintentos configurables
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofMillis(500);

    public KeycloakAdapter(WebClient.Builder webClientBuilder, @Value("${keycloak.url}") String keycloakUrl) {
        // Forzar el uso del resolvedor de DNS del sistema (JDK) para evitar errores en Windows/Netty
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        this.webClient = webClientBuilder
                .baseUrl(keycloakUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Mono<AuthTokens> login(String username, String password, String realm) {
        log.debug("Intentando login en Keycloak - Usuario: {}, Realm: {}", username, realm);

        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(buildRetrySpec("login"))
                .onErrorMap(reactor.core.Exceptions::isRetryExhausted, Throwable::getCause)
                .map(this::mapToDomain)
                .doOnSuccess(t -> log.info("Login exitoso en Keycloak para usuario: {}", username))
                .onErrorResume(e -> {
                    String detailedMsg = extractKeycloakErrorMessage(e, "login");
                    log.error("Error definitivo en login de Keycloak para {}: {}", username, detailedMsg);
                    return Mono.error(new RuntimeException(detailedMsg));
                });
    }

    @Override
    public Mono<AuthTokens> refreshToken(String refreshToken, String realm) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(buildRetrySpec("refreshToken"))
                .onErrorMap(reactor.core.Exceptions::isRetryExhausted, Throwable::getCause)
                .map(this::mapToDomain)
                .onErrorResume(e -> {
                    String detailedMsg = extractKeycloakErrorMessage(e, "refresh token");
                    log.error("Error al renovar token en Keycloak: {}", detailedMsg);
                    return Mono.error(new RuntimeException(detailedMsg));
                });
    }

    @Override
    public Mono<Void> logout(String refreshToken, String realm) {
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(buildRetrySpec("logout"))
                .onErrorResume(e -> {
                    log.warn("Error al cerrar sesión en Keycloak (no bloqueante): {}", e.getMessage());
                    return Mono.empty();
                });
    }

    @Override
    public Mono<String> createUser(String username, String password, String realm) {
        return createUser(username, password, realm, java.util.Collections.emptyMap());
    }

    @Override
    public Mono<String> createUser(String username, String password, String realm, java.util.Map<String, String> attributes) {
        log.info("Creando usuario {} en Keycloak (Realm: {}, Atributos: {})", username, realm, attributes);
        
        return getAdminToken()
                .flatMap(token -> {
                    java.util.Map<String, Object> userBody = new java.util.HashMap<>();
                    userBody.put("username", username);
                    userBody.put("enabled", true);
                    
                    if (attributes != null && !attributes.isEmpty()) {
                        java.util.Map<String, java.util.List<String>> kcAttributes = new java.util.HashMap<>();
                        attributes.forEach((k, v) -> kcAttributes.put(k, java.util.List.of(v)));
                        userBody.put("attributes", kcAttributes);
                    }

                    java.util.Map<String, Object> credentials = new java.util.HashMap<>();
                    credentials.put("type", "password");
                    credentials.put("value", password);
                    credentials.put("temporary", false);
                    
                    userBody.put("credentials", java.util.List.of(credentials));

                    return webClient.post()
                            .uri("/admin/realms/{realm}/users", realm)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(userBody)
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(REQUEST_TIMEOUT)
                            .flatMap(response -> {
                                java.util.List<String> location = response.getHeaders().get("Location");
                                if (location != null && !location.isEmpty()) {
                                    String path = location.get(0);
                                    return Mono.just(path.substring(path.lastIndexOf("/") + 1));
                                }
                                return Mono.just("");
                            });
                })
                .retryWhen(buildRetrySpec("createUser"))
                .onErrorResume(e -> {
                    log.error("Error al crear usuario en Keycloak ({}): {}", username, e.getMessage());
                    return Mono.error(new RuntimeException("Error al crear usuario en el servicio de identidad: " + e.getMessage()));
                });
    }

    @Override
    public Mono<Void> updatePassword(String keycloakUserId, String newPassword, String realm) {
        log.info("Actualizando contraseña para usuario {} en Keycloak (Realm: {})", keycloakUserId, realm);
        return getAdminToken()
                .flatMap(token -> {
                    java.util.Map<String, Object> resetPasswordBody = new java.util.HashMap<>();
                    resetPasswordBody.put("type", "password");
                    resetPasswordBody.put("value", newPassword);
                    resetPasswordBody.put("temporary", false);

                    return webClient.put()
                            .uri("/admin/realms/{realm}/users/{userId}/reset-password", realm, keycloakUserId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(resetPasswordBody)
                            .retrieve()
                            .toBodilessEntity()
                            .timeout(REQUEST_TIMEOUT)
                            .then();
                })
                .retryWhen(buildRetrySpec("updatePassword"))
                .doOnError(e -> log.error("Error al actualizar contraseña en Keycloak: {}", e.getMessage()))
                .then();
    }

    @Override
    public Mono<Void> assignRole(String keycloakUserId, String roleName, String realm) {
        log.info("Asignando rol {} al usuario {} en Keycloak (Realm: {})", roleName, keycloakUserId, realm);
        return getAdminToken()
                .flatMap(token -> {
                    return webClient.get()
                            .uri("/admin/realms/{realm}/roles/{roleName}", realm, roleName)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(java.util.Map.class)
                            .timeout(REQUEST_TIMEOUT)
                            .flatMap(role -> {
                                return webClient.post()
                                        .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", realm, keycloakUserId)
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(java.util.List.of(role))
                                        .retrieve()
                                        .toBodilessEntity()
                                        .timeout(REQUEST_TIMEOUT)
                                        .then();
                            });
                })
                .retryWhen(buildRetrySpec("assignRole"))
                .doOnError(e -> log.error("Error al asignar rol {} en Keycloak: {}", roleName, e.getMessage()))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error en Keycloak al asignar rol " + roleName + ": " + e.getMessage())))
                .then();
    }

    @Override
    public Mono<Void> syncRoles(String keycloakUserId, java.util.List<String> roleNames, String realm) {
        log.info("Sincronizando roles para el usuario {} en Keycloak: {}", keycloakUserId, roleNames);
        return Flux.fromIterable(roleNames)
                .flatMap(roleName -> assignRole(keycloakUserId, roleName, realm))
                .then();
    }

    /**
     * Obtener token de administrador para operaciones de gestión.
     * Usa AtomicReference para thread-safety real en contexto reactivo
     * y reintentos con backoff para manejar caídas transitorias de Keycloak.
     */
    private Mono<String> getAdminToken() {
        // Verificar caché de forma thread-safe
        String cached = cachedAdminToken.get();
        if (cached != null && System.currentTimeMillis() < tokenExpiryTime) {
            return Mono.just(cached);
        }

        log.info("Solicitando nuevo token de administrador a Keycloak...");
        return webClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", adminRealm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", adminClientId)
                        .with("client_secret", adminClientSecret))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(buildRetrySpec("getAdminToken"))
                .map(response -> {
                    String newToken = response.getAccessToken();
                    this.cachedAdminToken.set(newToken);
                    // Restamos 30 segundos de margen para el vencimiento de la caché
                    this.tokenExpiryTime = System.currentTimeMillis() + (response.getExpiresIn() * 1000L) - 30000;
                    log.info("Token de administrador obtenido exitosamente (expira en {}s)", response.getExpiresIn());
                    return newToken;
                })
                .doOnError(e -> {
                    // Invalidar caché en caso de error para forzar re-solicitud
                    this.cachedAdminToken.set(null);
                    this.tokenExpiryTime = 0;
                    log.error("Fallo crítico al obtener token de administrador: {}", e.getMessage());
                });
    }

    /**
     * Construye la estrategia de reintentos para las operaciones con Keycloak.
     * NO reintenta errores de autenticación (401/403) ya que son definitivos.
     * SÍ reintenta errores de conexión, timeout y 5xx (Neon idle connections, Keycloak sobrecargado).
     */
    private Retry buildRetrySpec(String operationName) {
        return Retry.backoff(MAX_RETRIES, RETRY_MIN_BACKOFF)
                .maxBackoff(Duration.ofSeconds(5))
                .jitter(0.3)
                .filter(throwable -> {
                    // NO reintentar errores de autenticación/autorización del usuario
                    if (throwable instanceof WebClientResponseException) {
                        int status = ((WebClientResponseException) throwable).getStatusCode().value();
                        // 401 (credenciales incorrectas) y 400 (bad request) son definitivos
                        if (status == 401 || status == 400 || status == 403 || status == 404) {
                            log.debug("[{}] Error {} - No se reintenta (error del cliente)", operationName, status);
                            return false;
                        }
                    }
                    // SÍ reintentar timeouts, errores de conexión y errores 5xx del servidor
                    return true;
                })
                .doBeforeRetry(signal ->
                        log.warn("[{}] Reintento #{} a Keycloak (causa: {})",
                                operationName,
                                signal.totalRetries() + 1,
                                signal.failure().getMessage()));
    }

    /**
     * Extrae un mensaje de error legible desde las excepciones de WebClient/Keycloak.
     */
    private String extractKeycloakErrorMessage(Throwable e, String operation) {
        if (e instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            String body = wcre.getResponseBodyAsString();

            if (status == 401) {
                return "Credenciales inválidas en Keycloak";
            }
            if (status == 400 && body.contains("invalid_grant")) {
                return "Credenciales inválidas o usuario deshabilitado en Keycloak";
            }
            if (status >= 500) {
                return "El servicio de identidad (Keycloak) no está disponible. Intente nuevamente en unos momentos.";
            }

            return "Error de Keycloak (" + status + "): " + body;
        }

        if (e instanceof java.util.concurrent.TimeoutException) {
            return "El servicio de identidad no respondió a tiempo. Intente nuevamente.";
        }

        if (e.getCause() instanceof java.net.ConnectException) {
            return "No se pudo conectar al servicio de identidad. Verifique que Keycloak esté ejecutándose.";
        }

        return "Error de comunicación con el servicio de identidad: " + e.getMessage();
    }

    private AuthTokens mapToDomain(KeycloakTokenResponse response) {
        return AuthTokens.builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiresIn(response.getExpiresIn())
                .tokenType(response.getTokenType())
                .build();
    }
}
