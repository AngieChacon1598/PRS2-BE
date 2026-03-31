package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pe.vallegrande.AuthenticationService.domain.exception.DuplicateResourceException;
import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.BlockUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.CreateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.SuspendUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UpdateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.UserService;
import edu.pe.vallegrande.AuthenticationService.application.util.DateTimeUtil;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CurrentUserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ExternalAuthPort;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.OnboardTenantCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleLink;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PersonPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.RolePort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.UserRolePort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ConfigServiceClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementación del servicio de gestión de usuarios */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserPort userPort;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final CurrentUserPort currentUserPort;
    private final ExternalAuthPort externalAuthPort;
    private final PersonPort personPort;
    private final RolePort rolePort;
    private final UserRolePort userRolePort;
    private final ConfigServiceClientPort configServiceClientPort;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${keycloak.realm}")
    private String defaultRealm;

    @Override
    public Mono<UserAccount> createUser(CreateUserCommand command) {
        log.info("Creando nuevo usuario: {}", command.getUsername());

        return userPort.existsByUsername(command.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                "Ya existe un usuario con el username: " + command.getUsername()));
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
                    UUID currentUserId = tuple.getT1().orElse(null);
                    UUID municipalCode = tuple.getT2().orElse(null);

                    // Convertir Map a String para almacenar en la base de datos
                    String preferencesAsString = convertMapToString(command.getPreferences());

                    UserAccount user = UserAccount.builder()
                            .id(UUID.randomUUID())
                            .username(command.getUsername())
                            .passwordHash(hashPassword(command.getPassword()))
                            .personId(command.getPersonId())
                            .areaId(command.getAreaId())
                            .positionId(command.getPositionId())
                            .directManagerId(command.getDirectManagerId())
                            .municipalCode(municipalCode)
                            .status(command.getStatus() != null ? command.getStatus()
                                    : "ACTIVE")
                            .loginAttempts(0)
                            .preferences(preferencesAsString)
                            .createdBy(currentUserId)
                            .createdAt(DateTimeUtil.nowInPeru())
                            .updatedAt(DateTimeUtil.nowInPeru())
                            .passwordLastChanged(DateTimeUtil.nowInPeru())
                            .requiresPasswordReset(command.getRequiresPasswordReset() != null
                                    ? command.getRequiresPasswordReset()
                                    : false)
                            .build();

                    return user;
                })
                .flatMap(user -> {
                    // Usar el realm por defecto (Soporte para Single Realm sipreb)
                    String realm = defaultRealm;

                    java.util.Map<String, String> attributes = new java.util.HashMap<>();
                    attributes.put("user_id", user.getId().toString());
                    if (user.getMunicipalCode() != null) {
                        attributes.put("municipal_code", user.getMunicipalCode().toString());
                    }
                    if (user.getAreaId() != null)
                        attributes.put("area_id", user.getAreaId().toString());
                    if (user.getPositionId() != null)
                        attributes.put("position_id", user.getPositionId().toString());
                    if (user.getDirectManagerId() != null)
                        attributes.put("direct_manager_id", user.getDirectManagerId().toString());

                    return externalAuthPort.createUser(command.getUsername(), command.getPassword(), realm, attributes)
                            .flatMap(keycloakId -> {
                                UserAccount userWithKeycloak = user.toBuilder()
                                        .keycloakId(keycloakId)
                                        .build();
                                return userPort.save(userWithKeycloak)
                                        .flatMap(savedUser -> autoAssignDefaultRoles(savedUser, realm));
                            });
                })
                .doOnSuccess(user -> log.info("Usuario creado exitosamente: {}", user.getUsername()))
                .doOnError(error -> log.error("Error al crear usuario: {}", error.getMessage()));
    }

    @Override
    public Flux<UserAccount> getAllUsers(String status, UUID areaId, UUID positionId, UUID managerId, UUID municipalCode) {
        log.info("Obteniendo usuarios con filtros - status: {}, area: {}, position: {}, manager: {}, municipalCode: {}",
                status, areaId, positionId, managerId, municipalCode);

        Flux<UserAccount> users = (municipalCode != null) 
                ? userPort.findAllByMunicipalCode(municipalCode)
                : userPort.findAll();

        // Aplicar filtros si están presentes
        if (status != null && !status.isEmpty()) {
            users = users.filter(user -> status.equalsIgnoreCase(user.getStatus()));
        }
        if (areaId != null) {
            users = users.filter(user -> areaId.equals(user.getAreaId()));
        }
        if (positionId != null) {
            users = users.filter(user -> positionId.equals(user.getPositionId()));
        }
        if (managerId != null) {
            users = users.filter(user -> managerId.equals(user.getDirectManagerId()));
        }

        return users;
    }

    @Override
    public Mono<UserAccount> getUserById(UUID id) {
        log.info("Obteniendo usuario por ID: {}", id);
        return userPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)));
    }

    @Override
    public Mono<UserAccount> getUserByUsername(String username) {
        log.info("Obteniendo usuario por username: {}", username);
        return userPort.findByUsername(username)
                .switchIfEmpty(
                        Mono.error(new ResourceNotFoundException("Usuario no encontrado con username: " + username)));
    }

    @Override
    public Mono<UserAccount> updateUser(UUID id, UpdateUserCommand command) {
        log.info("Actualizando usuario con ID: {}", id);
        log.debug("Datos recibidos: username={}, personId={}, areaId={}, positionId={}, directManagerId={}, status={}",
                command.getUsername(),
                command.getPersonId(),
                command.getAreaId(),
                command.getPositionId(),
                command.getDirectManagerId(),
                command.getStatus());

        return userPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(existingUser -> {
                    // Verificar si el username ya existe en otro usuario
                    if (!existingUser.getUsername().equals(command.getUsername())) {
                        return userPort.existsByUsernameAndIdNot(command.getUsername(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(
                                                new DuplicateResourceException("Ya existe un usuario con el username: "
                                                        + command.getUsername()));
                                    }
                                    return Mono.just(existingUser);
                                });
                    }
                    return Mono.just(existingUser);
                })
                .flatMap(existingUser -> currentUserPort.currentUserId()
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.ofNullable(existingUser.getUpdatedBy())) // Fallback al último que actualizó o null
                        .map(opt -> {
                            UUID currentUserId = opt.orElse(null);
                            // Convertir Map a String para almacenar en la base de datos
                            String preferencesAsString = command.getPreferences() != null
                                    ? convertMapToString(command.getPreferences())
                                    : existingUser.getPreferences();

                            // Validar campos obligatorios
                            if (command.getUsername() == null
                                    || command.getUsername().trim().isEmpty()) {
                                throw new IllegalArgumentException("El username es obligatorio");
                            }

                            boolean isPasswordChanged = command.getPassword() != null
                                    && !command.getPassword().trim().isEmpty();

                            UserAccount updatedUser = existingUser.toBuilder()
                                    .id(existingUser.getId())
                                    .username(command.getUsername().trim())
                                    .passwordHash(isPasswordChanged
                                            ? hashPassword(command.getPassword())
                                            : existingUser.getPasswordHash())
                                    .personId(command.getPersonId())
                                    .areaId(command.getAreaId())
                                    .positionId(command.getPositionId())
                                    .directManagerId(command.getDirectManagerId())
                                    .municipalCode(existingUser.getMunicipalCode())
                                    .status(command.getStatus() != null ? command.getStatus()
                                            : existingUser.getStatus())
                                    .lastLogin(existingUser.getLastLogin())
                                    .loginAttempts(
                                            existingUser.getLoginAttempts() != null ? existingUser.getLoginAttempts()
                                                    : 0)
                                    .blockedUntil(existingUser.getBlockedUntil())
                                    .preferences(preferencesAsString != null ? preferencesAsString : "{}")
                                    .createdBy(existingUser.getCreatedBy())
                                    .createdAt(existingUser.getCreatedAt())
                                    .updatedBy(currentUserId)
                                    .updatedAt(DateTimeUtil.nowInPeru())
                                    .passwordLastChanged(isPasswordChanged
                                            ? DateTimeUtil.nowInPeru()
                                            : existingUser.getPasswordLastChanged())
                                    .requiresPasswordReset(command.getRequiresPasswordReset() != null
                                            ? command.getRequiresPasswordReset()
                                            : (isPasswordChanged ? false : existingUser.getRequiresPasswordReset()))
                                    .build();

                            return updatedUser;
                        }))
                .flatMap(userPort::save)
                .flatMap(savedUser -> {
                    boolean isPasswordChanged = command.getPassword() != null
                            && !command.getPassword().trim().isEmpty();
                    // Solo sincronizamos con Keycloak si hay cambio de password y tenemos el
                    // KeycloakId
                    if (isPasswordChanged && savedUser.getKeycloakId() != null) {
                        return externalAuthPort
                                .updatePassword(savedUser.getKeycloakId(), command.getPassword(), defaultRealm)
                                .then(Mono.just(savedUser));
                    }
                    return Mono.just(savedUser);
                })
                .doOnSuccess(user -> log.info("Usuario actualizado exitosamente: {}", user.getUsername()))
                .doOnError(error -> {
                    log.error("Error al actualizar usuario con ID {}: {}", id, error.getMessage(), error);
                    if (error instanceof NullPointerException) {
                        log.error("NullPointerException detectado - Stack trace:", error);
                    }
                });
    }

    @Override
    public Mono<Void> deleteUser(UUID id, UUID updatedBy) {
        log.info("Eliminando usuario con ID: {}", id);
        if (updatedBy == null) {
            return currentUserPort.currentUserId()
                    .flatMap(currentUserId -> userPort.updateStatus(id, "INACTIVE", currentUserId));
        }
        return userPort.updateStatus(id, "INACTIVE", updatedBy);
    }

    @Override
    public Mono<UserAccount> restoreUser(UUID id, UUID updatedBy) {
        log.info("Restaurando usuario con ID: {}", id);
        if (updatedBy == null) {
            return currentUserPort.currentUserId()
                    .flatMap(currentUserId -> userPort.updateStatus(id, "ACTIVE", currentUserId))
                    .then(userPort.findById(id));
        }
        return userPort.updateStatus(id, "ACTIVE", updatedBy)
                .then(userPort.findById(id));
    }

    @Override
    public Mono<UserAccount> suspendUser(UUID id, SuspendUserCommand command) {
        log.info("Suspendiendo usuario con ID: {} hasta: {}", id, command.getSuspensionEnd());

        return Mono.zip(
                currentUserPort.currentUserId()
                        .defaultIfEmpty(UUID.fromString("00000000-0000-0000-0000-000000000000")),
                userPort.findById(id)
                        .switchIfEmpty(
                                Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id))))
                .flatMap(tuple -> {
                    UUID suspendedBy = tuple.getT1();
                    UserAccount user = tuple.getT2();

                    UserAccount updated = user.toBuilder()
                            .status("SUSPENDED")
                            .suspensionReason(command.getReason())
                            .suspensionStart(DateTimeUtil.nowInPeru())
                            .suspensionEnd(command.getSuspensionEnd())
                            .suspendedBy(suspendedBy)
                            .updatedAt(DateTimeUtil.nowInPeru())
                            .build();

                    return userPort.save(updated);
                })
                .doOnSuccess(user -> log.info("Usuario suspendido exitosamente: {}", user.getUsername()));
    }

    @Override
    public Mono<UserAccount> blockUser(UUID id, BlockUserCommand command) {
        log.info("Bloqueando usuario con ID: {}", id);

        return userPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(user -> {
                    LocalDateTime blockedUntil = command.getBlockedUntil() != null
                            ? command.getBlockedUntil()
                            : DateTimeUtil.addHours(DateTimeUtil.nowInPeru(),
                                    command.getDurationHours() != null ? command.getDurationHours() : 24);

                    UserAccount updated = user.toBuilder()
                            .blockReason(command.getReason())
                            .blockStart(DateTimeUtil.nowInPeru())
                            .blockedUntil(blockedUntil)
                            .updatedAt(DateTimeUtil.nowInPeru())
                            .build();

                    return userPort.save(updated);
                })
                .doOnSuccess(user -> log.info("Usuario bloqueado exitosamente: {}", user.getUsername()));
    }

    @Override
    public Mono<UserAccount> unblockUser(UUID id) {
        log.info("Desbloqueando usuario con ID: {}", id);
        return userPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + id)))
                .flatMap(user -> {
                    log.info("Usuario encontrado: {}, blockedUntil antes: {}, areaId: {}, positionId: {}",
                            user.getUsername(), user.getBlockedUntil(), user.getAreaId(), user.getPositionId());

                    // Solo modificar campos relacionados con el bloqueo
                    // No modificar: areaId, positionId, directManagerId, username, personId, etc.
                    UserAccount updated = user.toBuilder()
                            .blockedUntil(null)
                            .blockReason(null)
                            .blockStart(null)
                            .loginAttempts(0)
                            .updatedAt(DateTimeUtil.nowInPeru())
                            .build();

                    // Guardar el usuario completo preservando todos los demás campos
                    return userPort.save(updated)
                            .doOnSuccess(updatedUser -> log.info(
                                    "Usuario desbloqueado: {}, blockedUntil: {}, loginAttempts: {}, areaId: {}, positionId: {}",
                                    updatedUser.getUsername(),
                                    updatedUser.getBlockedUntil(),
                                    updatedUser.getLoginAttempts(),
                                    updatedUser.getAreaId(),
                                    updatedUser.getPositionId()));
                })
                .doOnSuccess(dto -> log.info(
                        "Usuario desbloqueado exitosamente: {} - Área preservada: {}, Cargo preservado: {}",
                        dto.getUsername(), dto.getAreaId(), dto.getPositionId()));
    }

    @Override
    public Flux<UserAccount> getBlockedUsers() {
        log.info("Obteniendo todos los usuarios bloqueados");
        LocalDateTime now = DateTimeUtil.nowInPeru();
        log.info("Hora actual (Perú): {}", DateTimeUtil.formatForDisplay(now));

        return userPort.findAll()
                .doOnNext(user -> {
                    if (user.getBlockedUntil() != null) {
                        log.debug("Usuario: {}, blockedUntil: {}, isAfter(now): {}",
                                user.getUsername(),
                                user.getBlockedUntil(),
                                user.getBlockedUntil().isAfter(now));
                    }
                })
                .filter(user -> user.getBlockedUntil() != null && user.getBlockedUntil().isAfter(now))
                .doOnComplete(() -> log.info("Consulta de usuarios bloqueados completada"));
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userPort.existsByUsername(username);
    }

    @Override
    public Mono<Void> updateLastLogin(UUID id) {
        return userPort.updateLastLogin(id, DateTimeUtil.nowInPeru());
    }

    @Override
    public Mono<Void> incrementLoginAttempts(UUID id) {
        return userPort.incrementLoginAttempts(id);
    }

    @Override
    public Mono<edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult> syncSingleUserToKeycloak(
            UUID userId) {
        log.info("Sincronizando usuario individual con Keycloak: {}", userId);
        return userPort.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado con ID: " + userId)))
                .flatMap(user -> {
                    String realm = defaultRealm;

                    java.util.Map<String, String> attributes = new java.util.HashMap<>();
                    attributes.put("user_id", user.getId().toString());
                    if (user.getMunicipalCode() != null) {
                        attributes.put("municipal_code", user.getMunicipalCode().toString());
                    }
                    if (user.getAreaId() != null)
                        attributes.put("area_id", user.getAreaId().toString());
                    if (user.getPositionId() != null)
                        attributes.put("position_id", user.getPositionId().toString());
                    if (user.getDirectManagerId() != null)
                        attributes.put("direct_manager_id", user.getDirectManagerId().toString());

                    return externalAuthPort.createUser(user.getUsername(), "Temp123*", realm, attributes)
                            .flatMap(keycloakId -> userPort.save(user.toBuilder().keycloakId(keycloakId).build()))
                            .map(u -> edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult.builder()
                                    .total(1)
                                    .synced(1)
                                    .failed(java.util.Collections.emptyList())
                                    .build())
                            .onErrorResume(e -> {
                                log.error("Error al sincronizar usuario {}: {}", user.getUsername(), e.getMessage());
                                return Mono.just(edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult
                                        .builder()
                                        .total(1)
                                        .synced(0)
                                        .failed(java.util.List.of(user.getUsername() + ": " + e.getMessage()))
                                        .build());
                            });
                });
    }

    @Override
    public Mono<edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult> syncUsersToKeycloak(
            UUID municipalCode) {
        log.info("Iniciando sincronización masiva para municipalidad: {}", municipalCode);

        return userPort.findAll()
                .filter(user -> municipalCode == null || municipalCode.equals(user.getMunicipalCode()))
                .filter(user -> user.getKeycloakId() == null) // Solo los que no tienen ID de Keycloak
                .flatMap(user -> syncSingleUserToKeycloak(user.getId()), 3) // Concurrencia limitada a 3
                .collectList()
                .map(results -> {
                    int total = results.size();
                    int synced = results.stream().mapToInt(edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult::getSynced).sum();
                    java.util.List<String> failed = results.stream()
                            .flatMap(r -> r.getFailed().stream())
                            .toList();

                    return edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult.builder()
                            .total(total)
                            .synced(synced)
                            .failed(failed)
                            .build();
                });
    }

    /**
     * Hash de password usando BCrypt
     */
    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Convertir Map<String, Object> a String
     */
    private String convertMapToString(Map<String, Object> map) {
        if (map == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Error convirtiendo Map a String: {}", e.getMessage());
            return "{}";
        }
    }

    @Override
    @Transactional
    public Mono<UserAccount> onboardTenant(OnboardTenantCommand command) {
        log.info("Iniciando Onboarding para tenant: {}", command.getMunicipalCode());

        // 1. Crear la Persona (Perfil administrativo de la muni)
        Person person = Person.builder()
                .id(UUID.randomUUID())
                .firstName(command.getAuthorityName())
                .lastName("Administrador Municipal")
                .documentTypeId(1) // Por defecto DNI/RUC
                .documentNumber(command.getMunicipalCode().toString().substring(0, 8))
                .personType("N")
                .municipalCode(command.getMunicipalCode())
                .personalEmail(command.getEmail())
                .status(true)
                .build();

        return personPort.save(person)
                .flatMap(savedPerson -> {
                    // 2. Crear el Usuario
                    UserAccount userAccount = UserAccount.builder()
                            .id(UUID.randomUUID())
                            .username(command.getAdminUsername())
                            .passwordHash(hashPassword(command.getAdminPassword()))
                            .personId(savedPerson.getId())
                            .municipalCode(command.getMunicipalCode())
                            .status("ACTIVE")
                            .createdAt(DateTimeUtil.nowInPeru())
                            .build();

                    return userPort.save(userAccount);
                })
                .flatMap(savedUser -> {
                    // 3. Buscar el Rol TENANT_ADMIN
                    return rolePort.findByName("TENANT_ADMIN")
                            .switchIfEmpty(Mono.error(new RuntimeException(
                                    "Rol TENANT_ADMIN no encontrado. Asegúrese de que el sistema esté inicializado.")))
                            .flatMap(role -> {
                                // 4. Asignar Rol localmente
                                UserRoleLink assignment = UserRoleLink.builder()
                                        .userId(savedUser.getId())
                                        .roleId(role.getId())
                                        .assignedAt(DateTimeUtil.nowInPeru())
                                        .active(true)
                                        .municipalCode(command.getMunicipalCode())
                                        .build();

                                return userRolePort.save(assignment)
                                        .then(Mono.just(savedUser))
                                        .zipWith(Mono.just(role));
                            });
                })
                .flatMap(tuple -> {
                    UserAccount user = tuple.getT1();
                    edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel role = tuple.getT2();

                    // 5. Sincronizar con Keycloak
                    java.util.Map<String, String> attributes = new java.util.HashMap<>();
                    attributes.put("user_id", user.getId().toString());
                    attributes.put("municipal_code", user.getMunicipalCode().toString());

                    return externalAuthPort
                            .createUser(user.getUsername(), command.getAdminPassword(), defaultRealm, attributes)
                            .flatMap(keycloakId -> {
                                UserAccount updatedWithKeycloak = user.toBuilder().keycloakId(keycloakId).build();
                                return userPort.save(updatedWithKeycloak)
                                        .flatMap(finalUser -> externalAuthPort
                                                .assignRole(keycloakId, role.getName(), defaultRealm)
                                                .thenReturn(finalUser));
                            });
                })
                .doOnSuccess(u -> log.info("Onboarding completado exitosamente para el usuario: {}", u.getUsername()))
                .doOnError(e -> log.error("Error durante el onboarding del tenant: {}", e.getMessage()));
    }

    /**
     * Consulta el microservicio de configuración para obtener los roles por defecto
     * asociados al cargo y área del usuario, y los asigna automáticamente.
     */
    private Mono<UserAccount> autoAssignDefaultRoles(UserAccount user, String realm) {
        if (user.getPositionId() == null) {
            return Mono.just(user);
        }

        log.info("Iniciando auto-asignación de roles para el usuario: {} (position: {}, area: {})", 
                user.getUsername(), user.getPositionId(), user.getAreaId());

        return configServiceClientPort.getDefaultRolesByContext(
                user.getPositionId(), user.getAreaId(), user.getMunicipalCode())
                .flatMap(roleId -> rolePort.findById(roleId)
                        .flatMap(role -> {
                            log.info("Auto-asignando rol: {} al usuario: {}", role.getName(), user.getUsername());
                            
                            UserRoleLink assignment = UserRoleLink.builder()
                                    .userId(user.getId())
                                    .roleId(role.getId())
                                    .assignedAt(DateTimeUtil.nowInPeru())
                                    .active(true)
                                    .municipalCode(user.getMunicipalCode())
                                    .build();

                            return userRolePort.save(assignment)
                                    .then(externalAuthPort.assignRole(user.getKeycloakId(), role.getName(), realm));
                        }))
                .then(Mono.just(user))
                .onErrorResume(e -> {
                    log.error("Error durante la auto-asignación de roles para {}: {}", user.getUsername(), e.getMessage());
                    return Mono.just(user); // No bloqueamos el flujo principal si falla la asignación automática
                });
    }
}