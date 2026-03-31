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

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RoleRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RoleResponseDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper.RoleWebMapper;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.RoleService;
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

/** Controlador REST para gestión de roles */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "API para la gestión de roles del sistema")
public class RoleController {

        private final RoleService roleService;

        @Operation(summary = "Crear un nuevo rol")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
                        @ApiResponse(responseCode = "409", description = "Ya existe un rol con ese nombre")
        })
        @PostMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RoleResponseDto>> createRole(
                        @jakarta.validation.Valid @RequestBody RoleRequestDto roleRequestDto) {
                log.info("Solicitud para crear rol: {}", roleRequestDto.getName());
                return roleService.createRole(RoleWebMapper.toCommand(roleRequestDto))
                                .map(role -> ResponseEntity.status(HttpStatus.CREATED).body(RoleWebMapper.toDto(role)));
        }

        @Operation(summary = "Obtener todos los roles", description = "Obtiene todos los roles. Opcionalmente puede filtrar por estado activo usando el parámetro 'active'")
        @GetMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<RoleResponseDto> getAllRoles(
                        @Parameter(description = "Filtrar por estado activo (true/false). Si no se especifica, devuelve todos") @RequestParam(required = false) Boolean active) {
                log.info("Solicitud para obtener roles con filtro active: {}", active);
                return roleService.getAllRoles(active).map(RoleWebMapper::toDto);
        }

        @Operation(summary = "Obtener rol por ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rol encontrado"),
                        @ApiResponse(responseCode = "404", description = "Rol no encontrado")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RoleResponseDto>> getRoleById(
                        @Parameter(description = "ID del rol") @PathVariable UUID id) {
                log.info("Solicitud para obtener rol por ID: {}", id);
                return roleService.getRoleById(id)
                                .map(role -> ResponseEntity.ok(RoleWebMapper.toDto(role)));
        }

        @Operation(summary = "Obtener rol por nombre")
        @GetMapping("/name/{name}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RoleResponseDto>> getRoleByName(
                        @Parameter(description = "Nombre del rol") @PathVariable String name) {
                log.info("Solicitud para obtener rol por nombre: {}", name);
                return roleService.getRoleByName(name)
                                .map(role -> ResponseEntity.ok(RoleWebMapper.toDto(role)));
        }

        @Operation(summary = "Actualizar un rol")
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RoleResponseDto>> updateRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID id,
                        @jakarta.validation.Valid @RequestBody RoleRequestDto roleRequestDto) {
                log.info("Solicitud para actualizar rol con ID: {}", id);
                return roleService.updateRole(id, RoleWebMapper.toCommand(roleRequestDto))
                                .map(role -> ResponseEntity.ok(RoleWebMapper.toDto(role)));
        }

        @Operation(summary = "Eliminar un rol (eliminación lógica)")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<Void>> deleteRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID id) {
                log.info("Solicitud para eliminar rol con ID: {}", id);
                return roleService.deleteRole(id)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
        }

        @Operation(summary = "Restaurar un rol eliminado")
        @PatchMapping("/{id}/restore")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<RoleResponseDto>> restoreRole(
                        @Parameter(description = "ID del rol") @PathVariable UUID id) {
                log.info("Solicitud para restaurar rol con ID: {}", id);
                return roleService.restoreRole(id)
                                .map(role -> ResponseEntity.ok(RoleWebMapper.toDto(role)));
        }
}
