package pe.edu.vallegrande.movementservice.infrastructure.adapters.output.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.movementservice.application.dto.UserResponse;
import pe.edu.vallegrande.movementservice.application.ports.output.UserServiceClientPort;
import pe.edu.vallegrande.movementservice.infrastructure.config.OutboundHttpProperties;
import pe.edu.vallegrande.movementservice.infrastructure.config.ReactiveHttpResilience;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class UserServiceClient implements UserServiceClientPort {

    private static final String CIRCUIT_BREAKER_ID = "userService";

    private final WebClient webClient;
    private final ReactiveCircuitBreaker circuitBreaker;
    private final ReactiveHttpResilience resilience;
    private final OutboundHttpProperties httpProperties;

    @SuppressWarnings("rawtypes")
    public UserServiceClient(
            @Value("${services.user.url:http://localhost:5002}") String userServiceBaseUrl,
            WebClient.Builder webClientBuilder,
            ReactiveCircuitBreakerFactory circuitBreakerFactory,
            ReactiveHttpResilience resilience,
            OutboundHttpProperties httpProperties) {
        this.webClient = webClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
        this.circuitBreaker = circuitBreakerFactory.create(CIRCUIT_BREAKER_ID);
        this.resilience = resilience;
        this.httpProperties = httpProperties;
    }

    @Override
    public Mono<UserResponse> getUserById(UUID userId) {
        if (userId == null) {
            return Mono.empty();
        }

        log.debug("Fetching user {} from User Service (MS-02)", userId);

        Mono<UserResponse> call = webClient.get()
                .uri("/api/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserResponse.class)
                .timeout(Duration.ofSeconds(httpProperties.getUserRequestTimeoutSeconds()))
                .doOnSuccess(user -> log.debug("Successfully fetched user {} from User Service (MS-02)", userId))
                .doOnError(error -> log.error("Failed to fetch user {} from User Service (MS-02)", userId, error))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("User {} not found in User Service (MS-02)", userId);
                    return Mono.empty();
                });

        return resilience.run(circuitBreaker, call, throwable -> {
                    if (throwable instanceof WebClientResponseException ex) {
                        log.error("User Service (MS-02) returned {} for user {}: {}",
                                ex.getStatusCode(), userId, ex.getResponseBodyAsString());
                    } else {
                        log.error("User Service (MS-02) unavailable, circuit open, or exhausted retries for user {}",
                                userId, throwable);
                    }
                    return Mono.empty();
                });
    }
}
