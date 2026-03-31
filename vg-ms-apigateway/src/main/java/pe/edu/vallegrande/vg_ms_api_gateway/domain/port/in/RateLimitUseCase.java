package pe.edu.vallegrande.vg_ms_api_gateway.domain.port.in;

import reactor.core.publisher.Mono;

public interface RateLimitUseCase {
    Mono<Boolean> checkLimit(String identifier);
}
