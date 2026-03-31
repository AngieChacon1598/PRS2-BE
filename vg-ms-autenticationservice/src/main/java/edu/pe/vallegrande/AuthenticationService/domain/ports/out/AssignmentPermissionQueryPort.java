package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import reactor.core.publisher.Flux;

public interface AssignmentPermissionQueryPort {
    Flux<PermissionModel> findUserPermissions(UUID userId);
}

