package pe.edu.vallegrande.configurationservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Bean
    public WebClient authWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(authServiceUrl)
                .filter((request, next) -> {
                    return Mono.deferContextual(contextView -> {
                        // Intentamos obtener el header Authorization del contexto de la petición
                        // original
                        return next.exchange(request);
                    });
                })
                // Esta es la forma más segura de capturar el token en Spring Security Reactive
                .filter((request, next) -> org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .getContext()
                    .filter(ctx -> !request.headers().containsKey(HttpHeaders.AUTHORIZATION))
                        .map(ctx -> ctx.getAuthentication().getCredentials().toString())
                        .map(token -> ClientRequest.from(request)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build())
                        .defaultIfEmpty(request)
                        .flatMap(next::exchange))
                .build();
    }
}