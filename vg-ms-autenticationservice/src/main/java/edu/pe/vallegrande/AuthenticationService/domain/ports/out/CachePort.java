package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.List;
import reactor.core.publisher.Mono;

public interface CachePort {
    Mono<List<String>> getPermissions(String userId, String municipalCode);
    Mono<Void> setPermissions(String userId, String municipalCode, List<String> permissions);
    Mono<Void> invalidate(String userId, String municipalCode);
}
