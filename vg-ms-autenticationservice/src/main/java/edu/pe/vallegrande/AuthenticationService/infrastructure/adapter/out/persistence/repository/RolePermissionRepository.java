package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.RolePermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio para la gestión de asignaciones rol-permiso
 */
@Repository
public interface RolePermissionRepository extends R2dbcRepository<RolePermission, UUID> {

        /**
         * Buscar permisos asignados a un rol
         */
        Flux<RolePermission> findByRoleId(UUID roleId);

        /**
         * Buscar roles que tienen un permiso específico
         */
        Flux<RolePermission> findByPermissionId(UUID permissionId);

        /**
         * Buscar asignación específica rol-permiso
         */
        Mono<RolePermission> findByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

        /**
         * Verificar si existe asignación rol-permiso
         */
        Mono<Boolean> existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

        /**
         * Eliminar asignación rol-permiso
         */
        Mono<Void> deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

        /**
         * Obtener permisos con información completa para un rol
         */
        @Query("""
                        SELECT rp.role_id, r.name as role_name, rp.permission_id, p.module, p.action, p.resource, p.description, rp.created_at
                        FROM roles_permissions rp
                        JOIN roles r ON rp.role_id = r.id
                        JOIN permissions p ON rp.permission_id = p.id
                        WHERE rp.role_id = :roleId
                        """)
        Flux<RolePermission> findRolePermissionsWithDetails(UUID roleId);

        /**
         * Obtener roles con información completa para un permiso
         */
        @Query("""
                        SELECT rp.role_id, r.name as role_name, rp.permission_id, p.module, p.action, p.resource, p.description, rp.created_at
                        FROM roles_permissions rp
                        JOIN roles r ON rp.role_id = r.id
                        JOIN permissions p ON rp.permission_id = p.id
                        WHERE rp.permission_id = :permissionId
                        """)
        Flux<RolePermission> findPermissionRolesWithDetails(UUID permissionId);

        /**
         * Obtener todos los permisos de un usuario a través de sus roles
         */
        @Query("""
                        SELECT DISTINCT rp.role_id, r.name as role_name, rp.permission_id, p.module, p.action, p.resource, p.description, rp.created_at
                        FROM users_roles ur
                        JOIN roles_permissions rp ON ur.role_id = rp.role_id
                        JOIN roles r ON rp.role_id = r.id
                        JOIN permissions p ON rp.permission_id = p.id
                        WHERE ur.user_id = :userId AND ur.active = true
                        """)
        Flux<RolePermission> findUserPermissions(UUID userId);
}