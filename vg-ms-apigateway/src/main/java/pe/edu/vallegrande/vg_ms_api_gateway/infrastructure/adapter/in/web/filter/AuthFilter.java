package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.in.web.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.model.AuthToken;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.ValidateTokenUseCase;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final ValidateTokenUseCase validateTokenUseCase;

    private static final List<String> AUTH_WHITELIST = List.of(
            "/api/v1/auth",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        
        // Log para debuggear en Dokploy logs
        System.out.println("DEBUG Gateway - Path recibido: " + path);

        // Si está en la lista blanca, permitimos el paso directamente
        boolean isWhitelisted = AUTH_WHITELIST.stream().anyMatch(whitelistItem -> 
            path.toLowerCase().contains(whitelistItem.toLowerCase()));
        
        if (isWhitelisted) {
            System.out.println("DEBUG Gateway - Whitelist detectada para: " + path);
            return chain.filter(exchange);
        }

        System.out.println("DEBUG Gateway - NO whitelisted, pidiendo token para: " + path);

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        AuthToken token = new AuthToken(authHeader.substring(7));
        return validateTokenUseCase.validate(token)
                .flatMap(isValid -> {
                    if (isValid) {
                        return chain.filter(exchange);
                    } else {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                });
    }

    @Override
    public int getOrder() {
        return -100; // Prioridad alta
    }
}
