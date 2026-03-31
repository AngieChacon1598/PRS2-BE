package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserCreateRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserResponseDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserUpdateRequestDto;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.OnboardTenantCommand;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.TenantOnboardingRequestDto;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.UserService;
import edu.pe.vallegrande.AuthenticationService.application.util.DateTimeUtil;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper.UserWebMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SyncUsersRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SyncResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Controlador REST para gestión de usuarios */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para la gestión de usuarios del sistema")
public class UserController {

        private final UserService userService;

        @Operation(summary = "Crear un nuevo usuario")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
                        @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese username")
        })
        @PostMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> createUser(
                        @jakarta.validation.Valid @RequestBody UserCreateRequestDto userCreateRequestDto) {
                log.info("Solicitud para crear usuario: {}", userCreateRequestDto.getUsername());
                return userService.createUser(UserWebMapper.toCommand(userCreateRequestDto))
                                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Obtener todos los usuarios con filtros opcionales", description = "Permite filtrar por status (ACTIVE/INACTIVE/SUSPENDED), área, posición, manager o usuarios bloqueados")
        @GetMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<UserResponseDto> getAllUsers(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "Filtrar por status (ACTIVE, INACTIVE, SUSPENDED)") @RequestParam(required = false) String status,
                        @Parameter(description = "Filtrar por área") @RequestParam(required = false) UUID areaId,
                        @Parameter(description = "Filtrar por posición") @RequestParam(required = false) UUID positionId,
                        @Parameter(description = "Filtrar por manager") @RequestParam(required = false) UUID managerId,
                        @Parameter(description = "Filtrar solo usuarios bloqueados (true/false)") @RequestParam(required = false) Boolean blocked,
                        @Parameter(description = "Número de página (opcional)") @RequestParam(required = false) Integer page,
                        @Parameter(description = "Tamaño de página (opcional)") @RequestParam(required = false) Integer size) {
                
                String mcStr = (jwt != null) ? jwt.getClaimAsString("municipal_code") : null;
                UUID municipalCode = (mcStr != null) ? UUID.fromString(mcStr) : null;

                log.info("Solicitud para obtener usuarios - status: {}, area: {}, position: {}, manager: {}, blocked: {}, municipalCode: {}",
                                status, areaId, positionId, managerId, blocked, municipalCode);

                Flux<UserResponseDto> users = userService.getAllUsers(status, areaId, positionId, managerId, municipalCode)
                                .map(UserWebMapper::toDto);

                // Filtrar usuarios bloqueados si se especifica
                if (blocked != null && blocked) {
                        users = users.filter(user -> user.getBlockedUntil() != null &&
                                        DateTimeUtil.isFuture(user.getBlockedUntil()));
                }

                // Aplicar paginación si se especifica
                if (page != null && size != null) {
                        users = users.skip((long) page * size).take(size);
                }

                return users;
        }

        @Operation(summary = "Obtener usuario por ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
                        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> getUserById(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id) {
                log.info("Solicitud para obtener usuario por ID: {}", id);
                return userService.getUserById(id)
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Obtener usuario por username")
        @GetMapping("/username/{username}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> getUserByUsername(
                        @Parameter(description = "Username del usuario") @PathVariable String username) {
                log.info("Solicitud para obtener usuario por username: {}", username);
                return userService.getUserByUsername(username)
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Actualizar un usuario")
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> updateUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @jakarta.validation.Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
                log.info("Solicitud para actualizar usuario con ID: {}", id);
                return userService.updateUser(id, UserWebMapper.toCommand(userUpdateRequestDto))
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Eliminar un usuario (eliminación lógica)")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Void>> deleteUser(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @Parameter(description = "ID del usuario que hace la eliminación (opcional)") @RequestParam(required = false) UUID updatedBy) {
                log.info("Solicitud para eliminar usuario con ID: {}", id);
                UUID finalUpdatedBy = updatedBy;
                if (finalUpdatedBy == null && jwt != null) {
                        String userIdStr = jwt.getClaimAsString("user_id");
                        if (userIdStr != null) finalUpdatedBy = UUID.fromString(userIdStr);
                }
                return userService.deleteUser(id, finalUpdatedBy)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        }

        @Operation(summary = "Restaurar un usuario eliminado")
        @PatchMapping("/{id}/restore")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> restoreUser(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @Parameter(description = "ID del usuario que hace la restauración (opcional)") @RequestParam(required = false) UUID updatedBy) {
                log.info("Solicitud para restaurar usuario con ID: {}", id);
                UUID finalUpdatedBy = updatedBy;
                if (finalUpdatedBy == null && jwt != null) {
                        String userIdStr = jwt.getClaimAsString("user_id");
                        if (userIdStr != null) finalUpdatedBy = UUID.fromString(userIdStr);
                }
                return userService.restoreUser(id, finalUpdatedBy)
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Suspender un usuario con motivo y fecha")
        @PatchMapping("/{id}/suspend")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> suspendUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @jakarta.validation.Valid @RequestBody edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SuspendUserRequestDto request) {
                log.info("Solicitud para suspender usuario con ID: {}", id);
                return userService.suspendUser(id, UserWebMapper.toCommand(request))
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Bloquear usuario temporalmente con motivo y fecha")
        @PatchMapping("/{id}/block")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> blockUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @jakarta.validation.Valid @RequestBody edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.BlockUserRequestDto request) {
                log.info("Solicitud para bloquear usuario con ID: {}", id);
                return userService.blockUser(id, UserWebMapper.toCommand(request))
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Desbloquear usuario")
        @PatchMapping("/{id}/unblock")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserResponseDto>> unblockUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id) {
                log.info("Solicitud para desbloquear usuario con ID: {}", id);
                return userService.unblockUser(id)
                                .map(user -> ResponseEntity.ok(UserWebMapper.toDto(user)));
        }

        @Operation(summary = "Obtener todos los usuarios bloqueados", description = "Lista solo los usuarios que están actualmente bloqueados")
        @GetMapping("/blocked")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<UserResponseDto> getBlockedUsers(
                        @Parameter(description = "Número de página (opcional)") @RequestParam(required = false) Integer page,
                        @Parameter(description = "Tamaño de página (opcional)") @RequestParam(required = false) Integer size) {
                log.info("Solicitud para obtener usuarios bloqueados");

                Flux<UserResponseDto> blockedUsers = userService.getBlockedUsers().map(UserWebMapper::toDto);

                // Aplicar paginación si se especifica
                if (page != null && size != null) {
                        blockedUsers = blockedUsers.skip((long) page * size).take(size);
                }

                return blockedUsers;
        }

        @Operation(summary = "Verificar si existe un usuario por username")
        @GetMapping("/exists/{username}")
        @PreAuthorize("isAuthenticated()")
        public Mono<ResponseEntity<Boolean>> existsByUsername(
                        @Parameter(description = "Username del usuario") @PathVariable String username) {
                log.info("Verificando existencia de usuario con username: {}", username);
                return userService.existsByUsername(username)
                                .map(exists -> ResponseEntity.ok(exists));
        }

        @Operation(summary = "Debug - Validar datos de actualización")
        @PostMapping("/{id}/validate-update")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Map<String, Object>>> validateUpdateData(
                        @Parameter(description = "ID del usuario") @PathVariable UUID id,
                        @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
                log.info("Validando datos de actualización para usuario ID: {}", id);

                Map<String, Object> validation = new HashMap<>();
                validation.put("id", id);
                validation.put("username", userUpdateRequestDto.getUsername());
                validation.put("usernameValid", userUpdateRequestDto.getUsername() != null
                                && !userUpdateRequestDto.getUsername().trim().isEmpty());
                validation.put("personId", userUpdateRequestDto.getPersonId());
                validation.put("personIdValid", userUpdateRequestDto.getPersonId() != null);
                validation.put("password", userUpdateRequestDto.getPassword() != null ? "***PROVIDED***" : null);
                validation.put("passwordValid", userUpdateRequestDto.getPassword() == null
                                || userUpdateRequestDto.getPassword().length() >= 8);
                validation.put("areaId", userUpdateRequestDto.getAreaId());
                validation.put("positionId", userUpdateRequestDto.getPositionId());
                validation.put("directManagerId", userUpdateRequestDto.getDirectManagerId());
                validation.put("status", userUpdateRequestDto.getStatus());
                validation.put("preferences", userUpdateRequestDto.getPreferences());

                return Mono.just(ResponseEntity.ok(validation));
        }

        @Operation(summary = "Forzar desbloqueo de usuarios expirados", description = "Desbloquea manualmente todos los usuarios cuyo blocked_until ya pasó")
        @PostMapping("/force-unblock-expired")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Map<String, Object>>> forceUnblockExpired() {
                log.info("🔧 Forzando desbloqueo de usuarios expirados");

                java.time.LocalDateTime now = DateTimeUtil.nowInPeru();

                return userService.getAllUsers(null, null, null, null, null)
                                .filter(user -> user.getBlockedUntil() != null)
                                .filter(user -> DateTimeUtil.isPast(user.getBlockedUntil()))
                                .flatMap(user -> {
                                        log.info("Desbloqueando usuario expirado: {} (bloqueado hasta: {})",
                                                        user.getUsername(), user.getBlockedUntil());
                                        return userService.unblockUser(user.getId());
                                })
                                .collectList()
                                .map(unlockedUsers -> {
                                        Map<String, Object> response = new HashMap<>();
                                        response.put("message", "Usuarios desbloqueados exitosamente");
                                        response.put("count", unlockedUsers.size());
                                        response.put("users", unlockedUsers.stream()
                                                        .map(u -> u.getUsername())
                                                        .toList());
                                        response.put("timestamp", DateTimeUtil.formatForDisplay(now));
                                        return ResponseEntity.ok(response);
                                });
        }

        @Operation(summary = "Sincronizar usuarios con Keycloak", 
                  description = "Sincroniza usuarios con Keycloak. \n" +
                                "- **1-a-1**: Si se proporciona un `userId` en el cuerpo.\n" +
                                "- **Por Municipalidad**: Si no se envía `userId`, sincroniza por `municipalCode` (del cuerpo o extraído del token).\n" +
                                "- **Global**: Si no hay `userId` ni `municipalCode` (sincroniza todos los usuarios sin KeycloakId).")
        @PostMapping("/sync")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
        public Mono<ResponseEntity<SyncResultDto>> syncUsers(
                        @AuthenticationPrincipal Jwt jwt,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false, 
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SyncUsersRequestDto.class)
                            ))
                        @RequestBody(required = false) SyncUsersRequestDto dto) {
                
                SyncUsersRequestDto finalDto = (dto != null) ? dto : new SyncUsersRequestDto();

                // Automatizar municipalCode si no viene en el body
                if (finalDto.getMunicipalCode() == null && jwt != null) {
                        String mcStr = jwt.getClaimAsString("municipal_code");
                        if (mcStr != null) {
                                finalDto.setMunicipalCode(UUID.fromString(mcStr));
                        }
                }

                log.info("Solicitud para sincronizar usuarios: user={}, municipal={}", 
                                finalDto.getUserId(), finalDto.getMunicipalCode());

                if (finalDto.getUserId() != null) {
                        return userService.syncSingleUserToKeycloak(finalDto.getUserId())
                                        .map(r -> ResponseEntity.ok(UserWebMapper.toSyncDto(r)));
                }

                return userService.syncUsersToKeycloak(finalDto.getMunicipalCode())
                                .map(r -> ResponseEntity.ok(UserWebMapper.toSyncDto(r)));
        }

        @Operation(summary = "Onboarding de nuevo Tenant", 
                  description = "Endpoint especializado para el Microservicio de Municipalidades. Crea persona, usuario y asigna rol de administrador en un solo flujo.")
        @PostMapping("/onboarding")
        @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ONBOARDING_MANAGER')")
        public Mono<ResponseEntity<UserResponseDto>> onboardTenant(
                        @jakarta.validation.Valid @RequestBody TenantOnboardingRequestDto dto) {
                log.info("🚀 Iniciando onboarding para municipalidad: {}", dto.getMunicipalCode());
                
                OnboardTenantCommand command = OnboardTenantCommand.builder()
                                .adminUsername(dto.getAdminUsername())
                                .adminPassword(dto.getAdminPassword())
                                .municipalCode(dto.getMunicipalCode())
                                .authorityName(dto.getAuthorityName())
                                .email(dto.getEmail())
                                .build();
                                
                return userService.onboardTenant(command)
                                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(UserWebMapper.toDto(user)));
        }
}
