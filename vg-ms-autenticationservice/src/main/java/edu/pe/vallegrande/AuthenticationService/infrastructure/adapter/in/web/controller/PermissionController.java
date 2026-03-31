package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

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
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PermissionRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PermissionResponseDto;
import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Controlador REST para gestión de permisos */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "API para la gestión de permisos del sistema")
public class PermissionController {

        private final PermissionService permissionService;

        /** Crear un nuevo permiso */
        @Operation(summary = "Crear nuevo permiso", description = "Crea un nuevo permiso en el sistema con módulo, acción y recurso específicos")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Permiso creado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @ApiResponse(responseCode = "409", description = "El permiso ya existe")
        })
        @PostMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PermissionResponseDto>> createPermission(
                        @Parameter(description = "Datos del permiso a crear", required = true) @Valid @RequestBody PermissionRequestDto permissionRequestDto) {

                log.info("Creando nuevo permiso: {}:{}:{}",
                                permissionRequestDto.getModule(),
                                permissionRequestDto.getAction(),
                                permissionRequestDto.getResource());

                return edu.pe.vallegrande.AuthenticationService.infrastructure.security.SecurityUtils.getCurrentUserId()
                                .flatMap(currentUserId -> edu.pe.vallegrande.AuthenticationService.infrastructure.security.SecurityUtils
                                                .getCurrentUserMunicipalCode()
                                                .map(municipalCode -> PermissionModel.builder()
                                                                .module(permissionRequestDto.getModule())
                                                                .action(permissionRequestDto.getAction())
                                                                .resource(permissionRequestDto.getResource())
                                                                .displayName(permissionRequestDto.getDisplayName())
                                                                .description(permissionRequestDto.getDescription())
                                                                .createdBy(currentUserId)
                                                                .municipalCode(municipalCode)
                                                                .build()))
                                .flatMap(permissionService::createPermission)
                                .map(this::mapToResponseDto)
                                .map(responseDto -> {
                                        log.info("Permiso creado exitosamente con ID: {}", responseDto.getId());
                                        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
                                })
                                .doOnError(error -> log.error("Error al crear permiso: {}", error.getMessage()));
        }

        /** Obtener permiso por ID */
        @Operation(summary = "Obtener permiso por ID", description = "Recupera la información completa de un permiso específico")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Permiso encontrado"),
                        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PermissionResponseDto>> getPermissionById(
                        @Parameter(description = "ID único del permiso", required = true) @PathVariable UUID id) {

                log.info("Consultando permiso con ID: {}", id);

                return permissionService.getPermissionById(id)
                                .map(this::mapToResponseDto)
                                .map(responseDto -> {
                                        log.info("Permiso encontrado: {}", responseDto.getId());
                                        return ResponseEntity.ok(responseDto);
                                })
                                .doOnError(error -> log.error("Error al consultar permiso {}: {}", id,
                                                error.getMessage()));
        }

        /** Obtener todos los permisos con filtros opcionales */
        @Operation(summary = "Listar todos los permisos", description = "Obtiene la lista de permisos con filtros opcionales por módulo y estado")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de permisos obtenida exitosamente")
        })
        @GetMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<PermissionResponseDto> getAllPermissions(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "Filtrar por módulo") @RequestParam(required = false) String module,
                        @Parameter(description = "Filtrar por estado (true/false)") @RequestParam(required = false) Boolean status) {
                
                String mcStr = (jwt != null) ? jwt.getClaimAsString("municipal_code") : null;
                UUID municipalCode = (mcStr != null) ? UUID.fromString(mcStr) : null;
                
                log.info("Consultando permisos con filtros - module: {}, status: {}, municipalCode: {}", module, status, municipalCode);

                return permissionService.getAllPermissions(module, status, municipalCode)
                                .map(this::mapToResponseDto)
                                .doOnComplete(() -> log.info("Consulta de permisos completada"))
                                .doOnError(error -> log.error("Error al consultar permisos: {}", error.getMessage()));
        }

        /** Actualizar permiso existente */
        @Operation(summary = "Actualizar permiso", description = "Actualiza la información de un permiso existente")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Permiso actualizado exitosamente"),
                        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PermissionResponseDto>> updatePermission(
                        @Parameter(description = "ID del permiso a actualizar", required = true) @PathVariable UUID id,
                        @Parameter(description = "Nuevos datos del permiso", required = true) @Valid @RequestBody PermissionRequestDto permissionRequestDto) {

                log.info("Actualizando permiso con ID: {}", id);

                PermissionModel permission = PermissionModel.builder()
                                .module(permissionRequestDto.getModule())
                                .action(permissionRequestDto.getAction())
                                .resource(permissionRequestDto.getResource())
                                .displayName(permissionRequestDto.getDisplayName())
                                .description(permissionRequestDto.getDescription())
                                .build();

                return permissionService.updatePermission(id, permission)
                                .map(this::mapToResponseDto)
                                .map(responseDto -> {
                                        log.info("Permiso actualizado exitosamente: {}", id);
                                        return ResponseEntity.ok(responseDto);
                                })
                                .doOnError(error -> log.error("Error al actualizar permiso {}: {}", id,
                                                error.getMessage()));
        }

        /** Eliminar permiso (borrado lógico) */
        @Operation(summary = "Eliminar permiso", description = "Elimina lógicamente un permiso del sistema (status=false)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Permiso eliminado exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Void>> deletePermission(
                        @Parameter(description = "ID del permiso a eliminar", required = true) @PathVariable UUID id) {

                log.info("Eliminando permiso con ID: {}", id);

                return permissionService.deletePermission(id)
                                .then(Mono.fromCallable(() -> {
                                        log.info("Permiso eliminado exitosamente: {}", id);
                                        return ResponseEntity.noContent().<Void>build();
                                }))
                                .doOnError(error -> log.error("Error al eliminar permiso {}: {}", id,
                                                error.getMessage()));
        }

        /** Restaurar permiso eliminado */
        @Operation(summary = "Restaurar permiso", description = "Restaura un permiso previamente eliminado (status=true)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Permiso restaurado exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
        })
        @PatchMapping("/{id}/restore")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PermissionResponseDto>> restorePermission(
                        @Parameter(description = "ID del permiso a restaurar", required = true) @PathVariable UUID id) {

                log.info("Restaurando permiso con ID: {}", id);

                return permissionService.restorePermission(id)
                                .map(this::mapToResponseDto)
                                .map(responseDto -> {
                                        log.info("Permiso restaurado exitosamente: {}", id);
                                        return ResponseEntity.ok(responseDto);
                                })
                                .doOnError(error -> log.error("Error al restaurar permiso {}: {}", id,
                                                error.getMessage()));
        }

        /** Buscar permiso por módulo, acción y recurso */
        @Operation(summary = "Buscar permiso por detalles", description = "Busca un permiso específico por módulo, acción y recurso")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Permiso encontrado"),
                        @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
        })
        @GetMapping("/search")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PermissionResponseDto>> getPermissionByDetails(
                        @Parameter(description = "Módulo del permiso", required = true) @RequestParam String module,
                        @Parameter(description = "Acción del permiso", required = true) @RequestParam String action,
                        @Parameter(description = "Recurso del permiso") @RequestParam(required = false) String resource) {

                log.info("Buscando permiso: {}:{}:{}", module, action, resource);

                return permissionService.getPermissionByDetails(module, action, resource)
                                .map(this::mapToResponseDto)
                                .map(responseDto -> {
                                        log.info("Permiso encontrado: {}", responseDto.getId());
                                        return ResponseEntity.ok(responseDto);
                                })
                                .doOnError(error -> log.error("Error al buscar permiso {}:{}:{}: {}",
                                                module, action, resource, error.getMessage()));
        }

        /** Obtener permisos asignados a un usuario */
        @Operation(summary = "Obtener permisos por usuario", description = "Recupera la lista de permisos (module:action:resource) asignados a un usuario")
        @GetMapping("/user/{userId}")
        @PreAuthorize("isAuthenticated()")
        public Flux<String> getUserPermissions(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "ID del usuario", required = true) @PathVariable UUID userId,
                        @Parameter(description = "Código municipal opcional") @RequestParam(required = false) UUID municipalCode) {
                
                UUID finalMunicipalCode = municipalCode;
                if (finalMunicipalCode == null && jwt != null) {
                        String mcStr = jwt.getClaimAsString("municipal_code");
                        if (mcStr != null) {
                                finalMunicipalCode = UUID.fromString(mcStr);
                        }
                }
                
                log.info("Consultando permisos granulares para usuario: {} con muni: {}", userId, finalMunicipalCode);
                return permissionService.getUserPermissions(userId, finalMunicipalCode);
        }

        /** Mapea Permission a PermissionResponseDto */
        private PermissionResponseDto mapToResponseDto(PermissionModel permission) {
                return PermissionResponseDto.builder()
                                .id(permission.getId())
                                .module(permission.getModule())
                                .action(permission.getAction())
                                .resource(permission.getResource())
                                .displayName(permission.getDisplayName())
                                .description(permission.getDescription())
                                .createdAt(permission.getCreatedAt())
                                .createdBy(permission.getCreatedBy())
                                .status(permission.getStatus())
                                .municipalCode(permission.getMunicipalCode())
                                .build();
        }
}
