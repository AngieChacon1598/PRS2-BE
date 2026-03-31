package pe.edu.vallegrande.vg_ms_api_gateway.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in.RateLimitUseCase;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.CachePort;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RateLimitService implements RateLimitUseCase {

    private final CachePort cachePort;
    private static final long MAX_REQUESTS = 100; // Configurable

    @Override
    public Mono<Boolean> checkLimit(String identifier) {
        String key = "ratelimit:" + identifier;
        return cachePort.increment(key)
                .map(count -> count <= MAX_REQUESTS);
    }
}
