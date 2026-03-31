package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.configservice;

import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ConfigServiceClientPort;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import io.netty.resolver.DefaultAddressResolverGroup;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class ConfigServiceAdapter implements ConfigServiceClientPort {

    private final WebClient webClient;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofMillis(500);

    public ConfigServiceAdapter(WebClient.Builder webClientBuilder, 
                                @Value("${services.configuration-service.url}") String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public Flux<UUID> getDefaultRolesByContext(UUID positionId, UUID areaId, UUID municipalityId) {
        log.info("Consultando roles por defecto en ConfigService: position={}, area={}, municipality={}", 
                positionId, areaId, municipalityId);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/position-allowed-roles/defaults-by-context")
                        .queryParam("positionId", positionId)
                        .queryParam("municipalityId", municipalityId)
                        .queryParamIfPresent("areaId", java.util.Optional.ofNullable(areaId))
                        .build())
                .retrieve()
                .bodyToFlux(PositionAllowedRoleDto.class)
                .timeout(REQUEST_TIMEOUT)
                .retryWhen(buildRetrySpec("getDefaultRolesByContext"))
                .map(PositionAllowedRoleDto::getRoleId)
                .onErrorResume(e -> {
                    log.error("Error definitivo al consultar ConfigService: {}", e.getMessage());
                    // Retornamos un Flux vacío como fallback o propagamos el error según necesidad
                    return Flux.empty(); 
                });
    }

    private Retry buildRetrySpec(String operationName) {
        return Retry.backoff(MAX_RETRIES, RETRY_MIN_BACKOFF)
                .maxBackoff(Duration.ofSeconds(5))
                .jitter(0.3)
                .filter(throwable -> !(throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest))
                .doBeforeRetry(signal ->
                        log.warn("[{}] Reintento #{} a ConfigService (causa: {})",
                                operationName,
                                signal.totalRetries() + 1,
                                signal.failure().getMessage()));
    }

    @Data
    private static class PositionAllowedRoleDto {
        private UUID roleId;
    }
}
