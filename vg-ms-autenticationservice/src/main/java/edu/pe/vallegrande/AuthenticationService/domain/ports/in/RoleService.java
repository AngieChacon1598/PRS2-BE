package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import edu.pe.vallegrande.AuthenticationService.domain.model.role.UpsertRoleCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interfaz del servicio para la gestión de roles
 */
public interface RoleService {

    /**
     * Crear un nuevo rol
     */
    Mono<RoleModel> createRole(UpsertRoleCommand command);

    /**
     * Obtener todos los roles con filtro opcional por estado activo
     * 
     * @param active Filtro opcional: true para activos, false para inactivos, null
     *               para todos
     */
    Flux<RoleModel> getAllRoles(Boolean active);

    /**
     * Obtener rol por ID
     */
    Mono<RoleModel> getRoleById(UUID id);

    /**
     * Obtener rol por nombre
     */
    Mono<RoleModel> getRoleByName(String name);

    /**
     * Actualizar un rol
     */
    Mono<RoleModel> updateRole(UUID id, UpsertRoleCommand command);

    /**
     * Eliminar un rol (eliminación lógica)
     */
    Mono<Void> deleteRole(UUID id);

    /**
     * Restaurar un rol eliminado
     */
    Mono<RoleModel> restoreRole(UUID id);
}