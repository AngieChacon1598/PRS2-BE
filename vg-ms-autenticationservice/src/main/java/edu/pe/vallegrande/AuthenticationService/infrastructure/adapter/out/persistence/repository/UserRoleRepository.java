package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.UserRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio para la gestión de asignaciones usuario-rol
 */
@Repository
public interface UserRoleRepository extends R2dbcRepository<UserRole, UUID> {

    /**
     * Buscar roles asignados a un usuario
     */
    Flux<UserRole> findByUserId(UUID userId);

    /**
     * Buscar roles activos asignados a un usuario
     */
    Flux<UserRole> findByUserIdAndActiveTrue(UUID userId);

    /**
     * Buscar usuarios con un rol específico
     */
    Flux<UserRole> findByRoleId(UUID roleId);

    /**
     * Buscar usuarios activos con un rol específico
     */
    Flux<UserRole> findByRoleIdAndActiveTrue(UUID roleId);

    /**
     * Buscar asignación específica usuario-rol
     */
    Mono<UserRole> findByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Verificar si existe asignación usuario-rol
     */
    Mono<Boolean> existsByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Eliminar asignación usuario-rol
     */
    Mono<Void> deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    /**
     * Buscar roles activos con fecha de expiración vencida
     */
    @Query("""
            SELECT * FROM users_roles
            WHERE active = true
            AND expiration_date IS NOT NULL
            AND expiration_date < CURRENT_DATE
            """)
    Flux<UserRole> findExpiredActiveRoles();

    /**
     * Obtener roles con información completa para un usuario
     */
    @Query("""
            SELECT ur.user_id, u.username, ur.role_id, r.name as role_name, r.description as role_description,
                   ur.assigned_by, ub.username as assigned_by_username, ur.assigned_at, ur.expiration_date, ur.active
            FROM users_roles ur
            JOIN users u ON ur.user_id = u.id
            JOIN roles r ON ur.role_id = r.id
            LEFT JOIN users ub ON ur.assigned_by = ub.id
            WHERE ur.user_id = :userId
            """)
    Flux<UserRole> findUserRolesWithDetails(UUID userId);

    /**
     * Obtener usuarios con información completa para un rol
     */
    @Query("""
            SELECT ur.user_id, u.username, ur.role_id, r.name as role_name, r.description as role_description,
                   ur.assigned_by, ub.username as assigned_by_username, ur.assigned_at, ur.expiration_date, ur.active
            FROM users_roles ur
            JOIN users u ON ur.user_id = u.id
            JOIN roles r ON ur.role_id = r.id
            LEFT JOIN users ub ON ur.assigned_by = ub.id
            WHERE ur.role_id = :roleId
            """)
    Flux<UserRole> findRoleUsersWithDetails(UUID roleId);
}