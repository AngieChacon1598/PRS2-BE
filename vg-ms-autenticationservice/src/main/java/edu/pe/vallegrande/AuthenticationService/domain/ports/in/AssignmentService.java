package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.AssignRoleCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionAssignment;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleAssignment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Servicio para la gestión de asignaciones usuario-rol y rol-permiso
 */
public interface AssignmentService {
    
    // === GESTIÓN USUARIO-ROL ===
    
    /**
     * Obtener roles asignados a un usuario
     */
    Flux<UserRoleAssignment> getUserRoles(UUID userId);
    
    /**
     * Asignar rol a usuario
     */
    Mono<UserRoleAssignment> assignRoleToUser(UUID userId, UUID roleId, AssignRoleCommand command);
    
    /**
     * Quitar rol de usuario
     */
    Mono<Void> removeRoleFromUser(UUID userId, UUID roleId);
    
    // === GESTIÓN ROL-PERMISO ===
    
    /**
     * Obtener permisos asignados a un rol
     */
    Flux<RolePermissionAssignment> getRolePermissions(UUID roleId);
    
    /**
     * Asignar permiso a rol
     */
    Mono<RolePermissionAssignment> assignPermissionToRole(UUID roleId, UUID permissionId);
    
    /**
     * Quitar permiso de rol (borrado lógico)
     */
    Mono<Void> removePermissionFromRole(UUID roleId, UUID permissionId);
    
    /**
     * Restaurar permiso de rol
     */
    Mono<RolePermissionAssignment> restorePermissionToRole(UUID roleId, UUID permissionId);
    
    // === CONSULTAS AVANZADAS ===
    
    /**
     * Obtener todos los permisos efectivos de un usuario
     */
    Flux<RolePermissionAssignment> getUserEffectivePermissions(UUID userId);
}