package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CurrentUserPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.security.SecurityUtils;
import reactor.core.publisher.Mono;

@Component
public class SecurityContextCurrentUserAdapter implements CurrentUserPort {
    @Override
    public Mono<UUID> currentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    @Override
    public Mono<UUID> currentMunicipalCode() {
        return SecurityUtils.getCurrentUserMunicipalCode();
    }
}

