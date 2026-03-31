package pe.edu.vallegrande.configurationservice.infrastructure.config;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.configurationservice.domain.exception.ResourceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class JwtContextHelper {

    public Mono<UUID> getMunicipalityId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (Jwt) ctx.getAuthentication().getPrincipal())
                .flatMap(jwt -> {
                    String municipalityId = jwt.getClaimAsString("municipal_code");
                    if (municipalityId == null || municipalityId.isBlank()) {
                        return Mono.error(new ResourceNotFoundException("municipal_code no encontrado en el token JWT"));
                    }
                    return Mono.just(UUID.fromString(municipalityId));
                });
    }

    public Mono<UUID> getUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> (Jwt) ctx.getAuthentication().getPrincipal())
                .map(jwt -> UUID.fromString(jwt.getSubject()));
    }
}
