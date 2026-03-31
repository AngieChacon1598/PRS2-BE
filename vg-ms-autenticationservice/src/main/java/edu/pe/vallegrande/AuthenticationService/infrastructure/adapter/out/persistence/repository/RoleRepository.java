package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio reactivo para la gestión de roles
 */
@Repository
public interface RoleRepository extends R2dbcRepository<Role, UUID> {
    
    /**
     * Buscar rol por nombre
     */
    Mono<Role> findByName(String name);
    
    /**
     * Buscar roles activos
     */
    Flux<Role> findByActiveTrue();
    
    /**
     * Buscar roles inactivos (eliminados lógicamente)
     */
    Flux<Role> findByActiveFalse();
    
    /**
     * Buscar roles del sistema
     */
    Flux<Role> findByIsSystemTrue();
    
    /**
     * Buscar roles no del sistema
     */
    Flux<Role> findByIsSystemFalse();
    
    /**
     * Verificar si existe un rol con el nombre dado
     */
    Mono<Boolean> existsByName(String name);
    
    /**
     * Verificar si existe un rol con el nombre dado excluyendo un ID específico
     */
    @Query("SELECT COUNT(*) > 0 FROM roles WHERE name = :name AND id != :id")
    Mono<Boolean> existsByNameAndIdNot(String name, UUID id);
    
    /**
     * Actualizar el estado activo de un rol (eliminación/restauración lógica)
     */
    @Query("UPDATE roles SET active = :active WHERE id = :id")
    Mono<Integer> updateActiveStatus(UUID id, Boolean active);
}