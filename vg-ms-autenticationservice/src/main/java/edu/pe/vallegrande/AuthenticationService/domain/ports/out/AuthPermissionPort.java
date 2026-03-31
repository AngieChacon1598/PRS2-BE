package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.UserPermission;
import reactor.core.publisher.Flux;

public interface AuthPermissionPort {
    Flux<UserPermission> findUserPermissions(UUID userId);
}

