package pe.edu.vallegrande.vg_ms_api_gateway.infrastructure.adapter.out.cache;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_api_gateway.domain.port.out.CachePort;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementación NO-OP del CachePort para desarrollo local.
 * No requiere Redis. El Rate Limiting queda deshabilitado.
 * En producción se reemplaza automáticamente por RedisCacheAdapter (profile "prod").
 */
@Component
@Profile("!prod")  // Activo en todos los perfiles EXCEPTO "prod"
public class NoOpCacheAdapter implements CachePort {

    @Override
    public Mono<Void> put(String key, String value, Duration ttl) {
        return Mono.empty(); // No hace nada
    }

    @Override
    public Mono<String> get(String key) {
        return Mono.empty(); // Siempre miss
    }

    @Override
    public Mono<Long> increment(String key) {
        return Mono.just(1L); // Siempre retorna 1 → nunca supera el límite
    }
}
