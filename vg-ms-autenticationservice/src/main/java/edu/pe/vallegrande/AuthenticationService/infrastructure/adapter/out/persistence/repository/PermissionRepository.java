package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Permission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio para la entidad Permission
 */
@Repository
public interface PermissionRepository extends R2dbcRepository<Permission, UUID> {
    
    /**
     * Obtiene los permisos efectivos de un usuario a través de sus roles
     */
    @Query("""
        SELECT p.* FROM permissions p
        INNER JOIN roles_permissions rp ON p.id = rp.permission_id
        INNER JOIN users_roles ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = :userId 
        AND ur.active = true
        AND (ur.expiration_date IS NULL OR ur.expiration_date > NOW())
        """)
    Flux<Permission> findUserPermissions(UUID userId);
    
    /**
     * Busca un permiso por módulo, acción y recurso
     */
    @Query("""
        SELECT * FROM permissions 
        WHERE module = :module 
        AND action = :action 
        AND (resource = :resource OR (:resource IS NULL AND resource IS NULL))
        """)
    Mono<Permission> findByModuleAndActionAndResource(String module, String action, String resource);
    
    /**
     * Obtiene permisos por módulo
     */
    @Query("SELECT * FROM permissions WHERE module = :module")
    Flux<Permission> findByModule(String module);
    
    /**
     * Verifica si existe un permiso con los detalles dados
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM permissions 
        WHERE module = :module 
        AND action = :action 
        AND (resource = :resource OR (:resource IS NULL AND resource IS NULL))
        """)
    Mono<Boolean> existsByModuleAndActionAndResource(String module, String action, String resource);
    
    /**
     * Obtiene permisos por estado
     */
    @Query("SELECT * FROM permissions WHERE status = :status")
    Flux<Permission> findByStatus(Boolean status);
    
    /**
     * Obtiene permisos por módulo y estado
     */
    @Query("SELECT * FROM permissions WHERE module = :module AND status = :status")
    Flux<Permission> findByModuleAndStatus(String module, Boolean status);

    Flux<Permission> findByMunicipalCode(UUID municipalCode);
}