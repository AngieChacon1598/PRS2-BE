package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.pe.vallegrande.AuthenticationService.domain.exception.DuplicateResourceException;
import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.AssignRoleCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionAssignment;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionLink;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleAssignment;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleLink;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.AssignmentService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.AssignmentPermissionQueryPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CurrentUserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PermissionPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePermissionPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserRolePort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ExternalAuthPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementación del servicio de asignaciones */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRolePort userRolePort;
    private final RolePermissionPort rolePermissionPort;
    private final AssignmentPermissionQueryPort assignmentPermissionQueryPort;
    private final UserPort userPort;
    private final RolePort rolePort;
    private final PermissionPort permissionPort;
    private final CurrentUserPort currentUserPort;
    private final ExternalAuthPort externalAuthPort;
 
    @org.springframework.beans.factory.annotation.Value("${keycloak.realm}")
    private String defaultRealm;

    // === GESTIÓN USUARIO-ROL ===

    @Override
    public Flux<UserRoleAssignment> getUserRoles(UUID userId) {
        log.info("Obteniendo roles del usuario: {}", userId);

        return userPort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + userId)))
                .flatMapMany(user -> userRolePort.findByUserId(userId)
                        .flatMap(this::mapUserRoleToDomain));
    }

    @Override
    public Mono<UserRoleAssignment> assignRoleToUser(UUID userId, UUID roleId, AssignRoleCommand command) {
        log.info("Asignando rol {} al usuario {}", roleId, userId);

        return userPort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + userId)))
                .then(rolePort.findById(roleId))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + roleId)))
                .then(userRolePort.existsByUserIdAndRoleId(userId, roleId))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException("El usuario ya tiene asignado este rol"));
                    }
                    return Mono.just(exists);
                })
                .then(Mono.zip(
                        currentUserPort.currentUserId()
                                .map(Optional::of)
                                .defaultIfEmpty(Optional.empty()),
                        currentUserPort.currentMunicipalCode()
                                .map(Optional::of)
                                .defaultIfEmpty(Optional.empty())))
                .map(tuple -> {
                    UUID assignedBy = tuple.getT1().orElse(null);
                    UUID municipalCode = tuple.getT2().orElse(null);

                    UserRoleLink userRole = UserRoleLink.builder()
                            .userId(userId)
                            .roleId(roleId)
                            .assignedBy(assignedBy)
                            .assignedAt(LocalDateTime.now())
                            .expirationDate(command.getExpirationDate())
                            .active(command.getActive() != null ? command.getActive() : true)
                            .municipalCode(municipalCode)
                            .build();

                    return userRole;
                })
                .flatMap(userRolePort::save)
                .flatMap(userRole -> {
                    log.info("Sincronizando con Keycloak para usuario {} y rol {}", userId, roleId);
                    // Sincronizar con Keycloak
                    return Mono.zip(
                            userPort.findById(userId),
                            rolePort.findById(roleId)
                    ).flatMap(tuple -> {
                        var user = tuple.getT1();
                        var role = tuple.getT2();
                        
                        log.info("User Keycloak ID: {}", user.getKeycloakId());
                        if (user.getKeycloakId() != null) {
                            return externalAuthPort.assignRole(user.getKeycloakId(), role.getName(), defaultRealm)
                                    .doOnSuccess(v -> log.info("Sincronización con Keycloak exitosa"))
                                    .doOnError(e -> log.error("Error al sincronizar con Keycloak: {}", e.getMessage()))
                                    .thenReturn(userRole)
                                    .onErrorReturn(userRole); // No romper la transacción si falla Keycloak
                        }
                        return Mono.just(userRole);
                    });
                })
                .flatMap(this::mapUserRoleToDomain)
                .doOnSuccess(assignment -> log.info("Rol asignado exitosamente y sincronizado con Keycloak: {} -> {}", userId, roleId));
    }

    @Override
    public Mono<Void> removeRoleFromUser(UUID userId, UUID roleId) {
        log.info("Quitando rol {} del usuario {}", roleId, userId);

        return userRolePort.existsByUserIdAndRoleId(userId, roleId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Asignación no encontrada"));
                    }
                    return userRolePort.deleteByUserIdAndRoleId(userId, roleId);
                })
                .doOnSuccess(unused -> log.info("Rol removido exitosamente: {} -> {}", userId, roleId));
    }

    // === GESTIÓN ROL-PERMISO ===

    @Override
    public Flux<RolePermissionAssignment> getRolePermissions(UUID roleId) {
        log.info("Obteniendo permisos del rol: {}", roleId);

        return rolePort.findById(roleId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + roleId)))
                .flatMapMany(role -> rolePermissionPort.findByRoleId(roleId)
                        .flatMap(this::mapRolePermissionToDomain));
    }

    @Override
    public Mono<RolePermissionAssignment> assignPermissionToRole(UUID roleId, UUID permissionId) {
        log.info("Asignando permiso {} al rol {}", permissionId, roleId);

        return rolePort.findById(roleId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + roleId)))
                .then(permissionPort.findById(permissionId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                                "Permiso no encontrado con ID: " + permissionId))))
                .then(rolePermissionPort.existsByRoleIdAndPermissionId(roleId, permissionId))
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException("El rol ya tiene asignado este permiso"));
                    }
                    return Mono.just(exists);
                })
                .then(currentUserPort.currentMunicipalCode()
                        .defaultIfEmpty(null))
                .map(municipalCode -> {
                    RolePermissionLink rolePermission = RolePermissionLink.builder()
                            .roleId(roleId)
                            .permissionId(permissionId)
                            .createdAt(LocalDateTime.now())
                            .municipalCode(municipalCode)
                            .build();

                    return rolePermission;
                })
                .flatMap(rolePermissionPort::save)
                .flatMap(this::mapRolePermissionToDomain)
                .doOnSuccess(assignment -> log.info("Permiso asignado exitosamente: {} -> {}", roleId, permissionId));
    }

    @Override
    public Mono<Void> removePermissionFromRole(UUID roleId, UUID permissionId) {
        log.info("Quitando permiso {} del rol {}", permissionId, roleId);

        return rolePermissionPort.existsByRoleIdAndPermissionId(roleId, permissionId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Asignación no encontrada"));
                    }
                    return rolePermissionPort.deleteByRoleIdAndPermissionId(roleId, permissionId);
                })
                .doOnSuccess(unused -> log.info("Permiso removido exitosamente: {} -> {}", roleId, permissionId));
    }

    @Override
    public Mono<RolePermissionAssignment> restorePermissionToRole(UUID roleId, UUID permissionId) {
        log.warn("Intento de restaurar permiso {} al rol {} - Operación no soportada con borrado físico", permissionId,
                roleId);
        return Mono.error(new UnsupportedOperationException(
                "La restauración de permisos no está disponible con borrado físico. Debe reasignar el permiso."));
    }

    // === CONSULTAS AVANZADAS ===

    @Override
    public Flux<RolePermissionAssignment> getUserEffectivePermissions(UUID userId) {
        log.info("Obteniendo permisos efectivos del usuario: {}", userId);

        return assignmentPermissionQueryPort.findUserPermissions(userId)
                .map(permission -> RolePermissionAssignment.builder()
                        .permissionId(permission.getId())
                        .module(permission.getModule())
                        .action(permission.getAction())
                        .resource(permission.getResource())
                        .description(permission.getDescription())
                        .createdAt(permission.getCreatedAt())
                        .build());
    }

    // === MÉTODOS AUXILIARES ===

    private Mono<UserRoleAssignment> mapUserRoleToDomain(UserRoleLink userRole) {
        return Mono.zip(
                userPort.findById(userRole.getUserId()),
                rolePort.findById(userRole.getRoleId())
        ).flatMap(tuple -> {
            var user = tuple.getT1();
            var role = tuple.getT2();

            Mono<String> assignedByUsernameMono = userRole.getAssignedBy() != null 
                    ? userPort.findById(userRole.getAssignedBy()).map(u -> u.getUsername()).defaultIfEmpty("Sistema")
                    : Mono.just("Sistema");

            return assignedByUsernameMono.map(username -> UserRoleAssignment.builder()
                    .userId(userRole.getUserId())
                    .username(user.getUsername())
                    .roleId(userRole.getRoleId())
                    .roleName(role.getName())
                    .roleDescription(role.getDescription())
                    .assignedBy(userRole.getAssignedBy())
                    .assignedByUsername(username)
                    .assignedAt(userRole.getAssignedAt())
                    .expirationDate(userRole.getExpirationDate())
                    .active(userRole.getActive())
                    .municipalCode(userRole.getMunicipalCode())
                    .build());
        });
    }

    private Mono<RolePermissionAssignment> mapRolePermissionToDomain(RolePermissionLink rolePermission) {
        return Mono.zip(
                rolePort.findById(rolePermission.getRoleId()),
                permissionPort.findById(rolePermission.getPermissionId()))
                .map(tuple -> {
                    var role = tuple.getT1();
                    var permission = tuple.getT2();

                    return RolePermissionAssignment.builder()
                            .roleId(rolePermission.getRoleId())
                            .roleName(role.getName())
                            .permissionId(rolePermission.getPermissionId())
                            .module(permission.getModule())
                            .action(permission.getAction())
                            .resource(permission.getResource())
                            .description(permission.getDescription())
                            .createdAt(rolePermission.getCreatedAt())
                            .municipalCode(rolePermission.getMunicipalCode())
                            .build();
                });
    }
}