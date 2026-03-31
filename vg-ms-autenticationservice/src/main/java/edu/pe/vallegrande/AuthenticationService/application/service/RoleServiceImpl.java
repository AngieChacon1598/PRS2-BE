package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.pe.vallegrande.AuthenticationService.domain.exception.DuplicateResourceException;
import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import edu.pe.vallegrande.AuthenticationService.domain.model.role.UpsertRoleCommand;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.RoleService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CurrentUserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementación del servicio de gestión de roles */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RolePort rolePort;
    private final CurrentUserPort currentUserPort;

    @Override
    public Mono<RoleModel> createRole(UpsertRoleCommand command) {
        log.info("Creando nuevo rol: {}", command.getName());

        return rolePort.existsByName(command.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                "Ya existe un rol con el nombre: " + command.getName()));
                    }
                    return Mono.just(exists);
                })
                .then(currentUserPort.currentUserId())
                .flatMap(currentUserId -> currentUserPort.currentMunicipalCode()
                        .map(municipalCode -> {
                            RoleModel role = RoleModel.builder()
                                    .name(command.getName())
                                    .description(command.getDescription())
                                    .isSystem(command.getIsSystem() != null ? command.getIsSystem() : false)
                                    .active(command.getActive() != null ? command.getActive() : true)
                                    .createdAt(LocalDateTime.now())
                                    .createdBy(currentUserId)
                                    .municipalCode(municipalCode)
                                    .build();

                            return role;
                        }))
                .flatMap(rolePort::save)
                .doOnSuccess(role -> log.info("Rol creado exitosamente: {}", role.getName()))
                .doOnError(error -> log.error("Error al crear rol: {}", error.getMessage()));
    }

    @Override
    public Flux<RoleModel> getAllRoles(Boolean active) {
        log.info("Obteniendo roles con filtro active: {}", active);

        if (active == null) {
            return rolePort.findAll()
                    .doOnComplete(() -> log.info("Todos los roles obtenidos exitosamente"));
        } else if (active) {
            return rolePort.findByActiveTrue()
                    .doOnComplete(() -> log.info("Roles activos obtenidos exitosamente"));
        } else {
            return rolePort.findByActiveFalse()
                    .doOnComplete(() -> log.info("Roles inactivos obtenidos exitosamente"));
        }
    }

    @Override
    public Mono<RoleModel> getRoleById(UUID id) {
        log.info("Obteniendo rol por ID: {}", id);
        return rolePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + id)))
                .doOnSuccess(role -> log.info("Rol encontrado: {}", role.getName()));
    }

    @Override
    public Mono<RoleModel> getRoleByName(String name) {
        log.info("Obteniendo rol por nombre: {}", name);
        return rolePort.findByName(name)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con nombre: " + name)))
                .doOnSuccess(role -> log.info("Rol encontrado: {}", role.getName()));
    }

    @Override
    public Mono<RoleModel> updateRole(UUID id, UpsertRoleCommand command) {
        log.info("Actualizando rol con ID: {}", id);

        return rolePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + id)))
                .flatMap(existingRole -> {
                    // Verificar si el nombre ya existe en otro rol
                    if (!existingRole.getName().equals(command.getName())) {
                        return rolePort.existsByNameAndIdNot(command.getName(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateResourceException(
                                                "Ya existe un rol con el nombre: " + command.getName()));
                                    }
                                    return Mono.just(existingRole);
                                });
                    }
                    return Mono.just(existingRole);
                })
                .flatMap(existingRole -> {
                    RoleModel updatedRole = existingRole.toBuilder()
                            .name(command.getName())
                            .description(command.getDescription())
                            .isSystem(command.getIsSystem() != null ? command.getIsSystem() : existingRole.getIsSystem())
                            .active(command.getActive() != null ? command.getActive() : existingRole.getActive())
                            .build();

                    return rolePort.save(updatedRole);
                })
                .doOnSuccess(role -> log.info("Rol actualizado exitosamente: {}", role.getName()))
                .doOnError(error -> log.error("Error al actualizar rol: {}", error.getMessage()));
    }

    @Override
    public Mono<Void> deleteRole(UUID id) {
        log.info("Eliminando rol con ID: {}", id);

        return rolePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + id)))
                .flatMap(role -> {
                    if (role.getIsSystem()) {
                        return Mono.error(new IllegalStateException("No se puede eliminar un rol del sistema"));
                    }
                    return rolePort.updateActiveStatus(id, false);
                })
                .then()
                .doOnSuccess(unused -> log.info("Rol eliminado exitosamente con ID: {}", id))
                .doOnError(error -> log.error("Error al eliminar rol: {}", error.getMessage()));
    }

    @Override
    public Mono<RoleModel> restoreRole(UUID id) {
        log.info("Restaurando rol con ID: {}", id);

        return rolePort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rol no encontrado con ID: " + id)))
                .flatMap(role -> {
                    if (role.getActive()) {
                        return Mono.error(new IllegalStateException("El rol ya está activo"));
                    }
                    return rolePort.updateActiveStatus(id, true)
                            .then(Mono.just(role.toBuilder().active(true).build()));
                })
                .doOnSuccess(role -> log.info("Rol restaurado exitosamente: {}", role.getName()))
                .doOnError(error -> log.error("Error al restaurar rol: {}", error.getMessage()));
    }
}