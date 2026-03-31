package pe.edu.vallegrande.movementservice.infrastructure.adapters.output.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.movementservice.application.dto.AssetUpdateRequest;
import pe.edu.vallegrande.movementservice.application.dto.MovementNotificationRequest;
import pe.edu.vallegrande.movementservice.application.ports.output.AssetServiceClientPort;
import pe.edu.vallegrande.movementservice.infrastructure.config.OutboundHttpProperties;
import pe.edu.vallegrande.movementservice.infrastructure.config.ReactiveHttpResilience;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class AssetServiceClient implements AssetServiceClientPort {

    private static final String CIRCUIT_BREAKER_ID = "patrimonioService";

    private final WebClient webClient;
    private final boolean integrationEnabled;
    private final ReactiveCircuitBreaker circuitBreaker;
    private final ReactiveHttpResilience resilience;
    private final OutboundHttpProperties httpProperties;

    @SuppressWarnings("rawtypes")
    public AssetServiceClient(
            @Value("${services.patrimonio.url:http://localhost:5004}") String patrimonioServiceBaseUrl,
            @Value("${services.patrimonio.enabled:false}") boolean integrationEnabled,
            WebClient.Builder webClientBuilder,
            ReactiveCircuitBreakerFactory circuitBreakerFactory,
            ReactiveHttpResilience resilience,
            OutboundHttpProperties httpProperties) {
        this.integrationEnabled = integrationEnabled;
        this.webClient = webClientBuilder
                .baseUrl(patrimonioServiceBaseUrl)
                .build();
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
        this.resilience = resilience;
        this.httpProperties = httpProperties;
    }

    @Override
    public Mono<Void> notifyNewMovement(MovementNotificationRequest notification) {
        if (!integrationEnabled) {
            log.debug("Patrimonio Service integration is disabled. Skipping notification for movement: {}",
                    notification.getMovementId());
            return Mono.empty();
        }

        log.info("Notifying Patrimonio Service (MS-04) about new movement: {} for bien: {}",
                notification.getMovementId(), notification.getAssetId());

        Mono<Void> call = webClient.post()
                .uri("/api/v1/bienes-patrimoniales/{bienId}/movement-notification", notification.getAssetId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notification)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(httpProperties.getPatrimonioRequestTimeoutSeconds()))
                .doOnSuccess(v -> log.info("Successfully notified Patrimonio Service (MS-04) about movement: {}",
                        notification.getMovementId()))
                .doOnError(error -> log.error("Failed to notify Patrimonio Service (MS-04) about movement: {}",
                        notification.getMovementId(), error));

        return resilience.run(circuitBreaker, call, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.warn("Patrimonio Service (MS-04) returned {} for movement {}: {}",
                                ex.getStatusCode(), notification.getMovementId(), ex.getResponseBodyAsString());
                    } else {
                        log.warn("Patrimonio Service (MS-04) unavailable or circuit open for movement {}: {}",
                                notification.getMovementId(), throwable.getMessage());
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> updateAssetLocation(AssetUpdateRequest updateRequest) {
        
        return Mono.empty();
    }

    public Mono<Void> updateAssetOnApproval(UUID bienId, UUID municipalityId, AssetUpdateRequest updateRequest) {
        if (!integrationEnabled) {
            log.warn("Patrimonio Service integration is DISABLED. Movement approval will proceed WITHOUT updating bien status. " +
                    "Enable it in application.yml: services.patrimonio.enabled=true");
            log.info("Would update bien {} status to {} for municipality {} (integration disabled)",
                    bienId, updateRequest.getAssetStatus(), municipalityId);
            return Mono.empty();
        }

        log.info("Updating bien {} status to {} for municipality {} via Patrimonio Service (MS-04)",
                bienId, updateRequest.getAssetStatus(), municipalityId);

        var statusChangeRequest = new java.util.HashMap<String, Object>();
        statusChangeRequest.put("nuevoEstado", updateRequest.getAssetStatus());
        statusChangeRequest.put("observaciones", updateRequest.getObservations() != null ? updateRequest.getObservations() : "Bien actualizado por aprobación de movimiento");

        Mono<Void> call = webClient.patch()
                .uri("/api/v1/assets/{id}/status", bienId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusChangeRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(httpProperties.getPatrimonioRequestTimeoutSeconds()))
                .doOnSuccess(v -> log.info("Successfully updated bien {} status to {} via Patrimonio Service (MS-04)",
                        bienId, updateRequest.getAssetStatus()))
                .doOnError(error -> log.error("Failed to update bien {} status via Patrimonio Service (MS-04)", bienId, error))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.error("Patrimonio Service (MS-04) returned error when updating bien {}: {} - {}",
                            bienId, ex.getStatusCode(), ex.getResponseBodyAsString());
                    return Mono.error(new RuntimeException(
                            "Failed to update bien status in Patrimonio Service (MS-04): " + ex.getMessage(), ex));
                });

        return resilience.run(circuitBreaker, call, Mono::error);
    }

    public Mono<Void> updateAssetOnCompletion(UUID bienId, UUID municipalityId, AssetUpdateRequest updateRequest) {
        if (!integrationEnabled) {
            log.debug("Patrimonio Service integration is disabled. Skipping completion update for bien: {}", bienId);
            return Mono.empty();
        }

        log.info("Updating bien {} on movement completion for municipality {} via Patrimonio Service (MS-04)",
                bienId, municipalityId);

        var statusChangeRequest = new java.util.HashMap<String, Object>();
        statusChangeRequest.put("nuevoEstado", updateRequest.getAssetStatus());
        statusChangeRequest.put("observaciones", updateRequest.getObservations() != null ? updateRequest.getObservations() : "Bien actualizado por completación de movimiento");

        Mono<Void> call = webClient.patch()
                .uri("/api/v1/assets/{id}/status", bienId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(statusChangeRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(httpProperties.getPatrimonioRequestTimeoutSeconds()))
                .doOnSuccess(v -> log.info("Successfully updated bien {} on completion via Patrimonio Service (MS-04)", bienId))
                .doOnError(error -> log.error("Failed to update bien {} on completion via Patrimonio Service (MS-04)", bienId, error));

        return resilience.run(circuitBreaker, call, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.warn("Patrimonio Service (MS-04) returned {} on completion for bien {}: {}",
                                ex.getStatusCode(), bienId, ex.getResponseBodyAsString());
                    } else {
                        log.warn("Patrimonio Service (MS-04) unavailable or circuit open on completion for bien {}: {}",
                                bienId, throwable.getMessage());
                    }
                    return Mono.empty();
                });
    }
}
