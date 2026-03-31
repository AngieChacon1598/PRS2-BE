package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import java.util.UUID;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.BlockUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.CreateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.SuspendUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UpdateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz del servicio para la gestión de usuarios
 */
public interface UserService {

    /**
     * Crear un nuevo usuario
     */
    Mono<UserAccount> createUser(CreateUserCommand command);

    /**
     * Obtener todos los usuarios con filtros opcionales
     */
    Flux<UserAccount> getAllUsers(String status, UUID areaId, UUID positionId, UUID managerId, UUID municipalCode);

    /**
     * Obtener usuario por ID
     */
    Mono<UserAccount> getUserById(UUID id);

    /**
     * Obtener usuario por username
     */
    Mono<UserAccount> getUserByUsername(String username);

    /**
     * Actualizar un usuario
     */
    Mono<UserAccount> updateUser(UUID id, UpdateUserCommand command);

    /**
     * Eliminar un usuario (cambio de status a INACTIVE)
     */
    Mono<Void> deleteUser(UUID id, UUID updatedBy);

    /**
     * Restaurar un usuario eliminado
     */
    Mono<UserAccount> restoreUser(UUID id, UUID updatedBy);

    /**
     * Suspender usuario con motivo y fecha
     */
    Mono<UserAccount> suspendUser(UUID id, SuspendUserCommand command);

    /**
     * Bloquear usuario temporalmente con motivo y fecha
     */
    Mono<UserAccount> blockUser(UUID id, BlockUserCommand command);

    /**
     * Desbloquear usuario
     */
    Mono<UserAccount> unblockUser(UUID id);

    /**
     * Obtener todos los usuarios bloqueados actualmente
     */
    Flux<UserAccount> getBlockedUsers();

    /**
     * Verificar si existe un usuario por username
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Actualizar último login
     */
    Mono<Void> updateLastLogin(UUID id);

    /**
     * Incrementar intentos de login fallidos
     */
    Mono<Void> incrementLoginAttempts(UUID id);

    Mono<edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult> syncSingleUserToKeycloak(UUID userId);

    Mono<edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult> syncUsersToKeycloak(UUID municipalCode);

    /**
     * Proceso de alta de una nueva municipalidad (crea persona, usuario y asigna rol de admin)
     */
    Mono<UserAccount> onboardTenant(edu.pe.vallegrande.AuthenticationService.domain.model.user.OnboardTenantCommand command);
}