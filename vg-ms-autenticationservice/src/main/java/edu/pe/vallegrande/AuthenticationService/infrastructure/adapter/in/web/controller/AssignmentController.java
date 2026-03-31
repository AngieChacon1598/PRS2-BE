package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.AssignRoleRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RolePermissionAssignmentDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserRoleAssignmentDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper.AssignmentWebMapper;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.AssignmentService;
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

/** Controlador REST para gestión de asignaciones usuario-rol y rol-permiso */
@Slf4j
@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "API para la gestión de asignaciones de roles y permisos")
public class AssignmentController {

        private final AssignmentService assignmentService;

        // === GESTIÓN USUARIO-ROL ===

        @Operation(summary = "Obtener roles asignados a un usuario")
        @GetMapping("/users/{userId}/roles")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<UserRoleAssignmentDto> getUserRoles(
                        @Parameter(description = "ID del usuario") @PathVariable UUID userId) {
                log.info("Solicitud para obtener roles del usuario: {}", userId);
                return assignmentService.getUserRoles(userId).map(AssignmentWebMapper::toDto);
        }

        @Operation(summary = "Asignar rol a usuario")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Rol asignado exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Usuario o rol no encontrado"),
                        @ApiResponse(responseCode = "409", description = "El usuario ya tiene este rol asignado")
        })
        @PostMapping("/users/{userId}/roles/{roleId}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<UserRoleAssignmentDto>> assignRoleToUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID userId,
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId,
                        @RequestBody AssignRoleRequestDto request) {
                log.info("Solicitud para asignar rol {} al usuario {}", roleId, userId);
                return assignmentService.assignRoleToUser(userId, roleId, AssignmentWebMapper.toCommand(request))
                                .map(AssignmentWebMapper::toDto)
                                .map(assignment -> ResponseEntity.status(HttpStatus.CREATED).body(assignment));
        }

        @Operation(summary = "Quitar rol de usuario")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Rol removido exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Asignación no encontrada")
        })
        @DeleteMapping("/users/{userId}/roles/{roleId}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Void>> removeRoleFromUser(
                        @Parameter(description = "ID del usuario") @PathVariable UUID userId,
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId) {
                log.info("Solicitud para quitar rol {} del usuario {}", roleId, userId);
                return assignmentService.removeRoleFromUser(userId, roleId)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        }

        // === GESTIÓN ROL-PERMISO ===

        @Operation(summary = "Obtener permisos asignados a un rol")
        @GetMapping("/roles/{roleId}/permissions")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<RolePermissionAssignmentDto> getRolePermissions(
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId) {
                log.info("Solicitud para obtener permisos del rol: {}", roleId);
                return assignmentService.getRolePermissions(roleId).map(AssignmentWebMapper::toDto);
        }

        @Operation(summary = "Asignar permiso a rol")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Permiso asignado exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Rol o permiso no encontrado"),
                        @ApiResponse(responseCode = "409", description = "El rol ya tiene este permiso asignado")
        })
        @PostMapping("/roles/{roleId}/permissions/{permissionId}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RolePermissionAssignmentDto>> assignPermissionToRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId,
                        @Parameter(description = "ID del permiso") @PathVariable UUID permissionId) {
                log.info("Solicitud para asignar permiso {} al rol {}", permissionId, roleId);
                return assignmentService.assignPermissionToRole(roleId, permissionId)
                                .map(AssignmentWebMapper::toDto)
                                .map(assignment -> ResponseEntity.status(HttpStatus.CREATED).body(assignment));
        }

        @Operation(summary = "Quitar permiso de rol (borrado lógico)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Permiso removido exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Asignación no encontrada")
        })
        @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Void>> removePermissionFromRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId,
                        @Parameter(description = "ID del permiso") @PathVariable UUID permissionId) {
                log.info("Solicitud para quitar permiso {} del rol {}", permissionId, roleId);
                return assignmentService.removePermissionFromRole(roleId, permissionId)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        }

        @Operation(summary = "Restaurar permiso de rol")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Permiso restaurado exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Asignación no encontrada")
        })
        @PatchMapping("/roles/{roleId}/permissions/{permissionId}/restore")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RolePermissionAssignmentDto>> restorePermissionToRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID roleId,
                        @Parameter(description = "ID del permiso") @PathVariable UUID permissionId) {
                log.info("Solicitud para restaurar permiso {} al rol {}", permissionId, roleId);
                return assignmentService.restorePermissionToRole(roleId, permissionId)
                                .map(AssignmentWebMapper::toDto)
                                .map(assignment -> ResponseEntity.ok(assignment));
        }

        // === CONSULTAS AVANZADAS ===

        @Operation(summary = "Obtener todos los permisos efectivos de un usuario")
        @GetMapping("/users/{userId}/effective-permissions")
        @PreAuthorize("isAuthenticated()")
        public Flux<RolePermissionAssignmentDto> getUserEffectivePermissions(
                        @Parameter(description = "ID del usuario") @PathVariable UUID userId) {
                log.info("Solicitud para obtener permisos efectivos del usuario: {}", userId);
                return assignmentService.getUserEffectivePermissions(userId).map(AssignmentWebMapper::toDto);
        }
}
