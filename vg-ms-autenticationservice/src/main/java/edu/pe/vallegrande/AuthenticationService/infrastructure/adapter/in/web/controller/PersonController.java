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
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PersonRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PersonResponseDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper.PersonWebMapper;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PersonService;
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

/** Controlador REST para gestión de personas */
@Slf4j
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
@Tag(name = "Persons", description = "API para la gestión de personas del sistema")
public class PersonController {

        private final PersonService personService;

        @Operation(summary = "Crear una nueva persona")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Persona creada exitosamente"),
                        @ApiResponse(responseCode = "409", description = "Ya existe una persona con ese documento o email")
        })
        @PostMapping
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PersonResponseDto>> createPerson(
                        @jakarta.validation.Valid @RequestBody PersonRequestDto personRequestDto) {
                log.info("Solicitud para crear persona: {} {}", personRequestDto.getFirstName(),
                                personRequestDto.getLastName());
                return personService.createPerson(PersonWebMapper.toCommand(personRequestDto))
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.status(HttpStatus.CREATED).body(person));
        }

        @Operation(summary = "Obtener todas las personas")
        @GetMapping
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Flux<PersonResponseDto> getAllPersons(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "Número de página (opcional)") @RequestParam(required = false) Integer page,
                        @Parameter(description = "Tamaño de página (opcional)") @RequestParam(required = false) Integer size) {
                
                String mcStr = (jwt != null) ? jwt.getClaimAsString("municipal_code") : null;
                UUID municipalCode = (mcStr != null) ? UUID.fromString(mcStr) : null;

                log.info("Solicitud para obtener todas las personas para muni: {} - page: {}, size: {}", municipalCode, page, size);

                Flux<PersonResponseDto> persons = personService.getAllPersons(municipalCode)
                                .map(PersonWebMapper::toDto);

                // Aplicar paginación si se especifica
                if (page != null && size != null) {
                        persons = persons.skip((long) page * size).take(size);
                }

                return persons;
        }

        @Operation(summary = "Obtener todas las personas activas")
        @GetMapping("/active")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Flux<PersonResponseDto> getAllActivePersons(
                        @AuthenticationPrincipal Jwt jwt) {
                String mcStr = (jwt != null) ? jwt.getClaimAsString("municipal_code") : null;
                UUID municipalCode = (mcStr != null) ? UUID.fromString(mcStr) : null;
                log.info("Solicitud para obtener todas las personas activas para muni: {}", municipalCode);
                return personService.getAllActivePersons(municipalCode)
                                .map(PersonWebMapper::toDto);
        }

        @Operation(summary = "Obtener todas las personas inactivas")
        @GetMapping("/inactive")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Flux<PersonResponseDto> getAllInactivePersons(
                        @AuthenticationPrincipal Jwt jwt) {
                String mcStr = (jwt != null) ? jwt.getClaimAsString("municipal_code") : null;
                UUID municipalCode = (mcStr != null) ? UUID.fromString(mcStr) : null;
                log.info("Solicitud para obtener todas las personas inactivas para muni: {}", municipalCode);
                return personService.getAllInactivePersons(municipalCode)
                                .map(PersonWebMapper::toDto);
        }

        @Operation(summary = "Obtener persona por ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Persona encontrada"),
                        @ApiResponse(responseCode = "404", description = "Persona no encontrada")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Mono<ResponseEntity<PersonResponseDto>> getPersonById(
                        @Parameter(description = "ID de la persona") @PathVariable UUID id) {
                log.info("Solicitud para obtener persona por ID: {}", id);
                return personService.getPersonById(id)
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Obtener persona por documento")
        @GetMapping("/document/{documentTypeId}/{documentNumber}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Mono<ResponseEntity<PersonResponseDto>> getPersonByDocument(
                        @Parameter(description = "Tipo de documento") @PathVariable Integer documentTypeId,
                        @Parameter(description = "Número de documento") @PathVariable String documentNumber) {
                log.info("Solicitud para obtener persona por documento: {} - {}", documentTypeId, documentNumber);
                return personService.getPersonByDocument(documentTypeId, documentNumber)
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Obtener persona por email")
        @GetMapping("/email/{email}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Mono<ResponseEntity<PersonResponseDto>> getPersonByEmail(
                        @Parameter(description = "Email personal") @PathVariable String email) {
                log.info("Solicitud para obtener persona por email: {}", email);
                return personService.getPersonByEmail(email)
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Buscar personas por nombre")
        @GetMapping("/search/name/{name}")
        @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'TENANT_CONFIG_MANAGER')")
        public Flux<PersonResponseDto> searchPersonsByName(
                        @Parameter(description = "Nombre a buscar") @PathVariable String name) {
                log.info("Solicitud para buscar personas por nombre: {}", name);
                return personService.searchPersonsByName(name)
                                .map(PersonWebMapper::toDto);
        }

        @Operation(summary = "Actualizar una persona")
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PersonResponseDto>> updatePerson(
                        @Parameter(description = "ID de la persona") @PathVariable UUID id,
                        @jakarta.validation.Valid @RequestBody PersonRequestDto personRequestDto) {
                log.info("Solicitud para actualizar persona con ID: {}", id);
                return personService.updatePerson(id, PersonWebMapper.toUpdateCommand(personRequestDto))
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Eliminar una persona (borrado lógico)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Persona eliminada exitosamente"),
                        @ApiResponse(responseCode = "404", description = "Persona no encontrada")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PersonResponseDto>> deletePerson(
                        @Parameter(description = "ID de la persona") @PathVariable UUID id) {
                log.info("Solicitud para eliminar persona con ID: {}", id);
                return personService.deletePerson(id)
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Restaurar una persona eliminada")
        @PatchMapping("/{id}/restore")
        @PreAuthorize("hasRole('TENANT_ADMIN')")
        public Mono<ResponseEntity<PersonResponseDto>> restorePerson(
                        @Parameter(description = "ID de la persona") @PathVariable UUID id) {
                log.info("Solicitud para restaurar persona con ID: {}", id);
                return personService.restorePerson(id)
                                .map(PersonWebMapper::toDto)
                                .map(person -> ResponseEntity.ok(person));
        }

        @Operation(summary = "Verificar si existe persona por documento")
        @GetMapping("/exists/document/{documentTypeId}/{documentNumber}")
        @PreAuthorize("isAuthenticated()")
        public Mono<ResponseEntity<Boolean>> existsByDocument(
                        @Parameter(description = "Tipo de documento") @PathVariable Integer documentTypeId,
                        @Parameter(description = "Número de documento") @PathVariable String documentNumber) {
                log.info("Verificando existencia de persona con documento: {} - {}", documentTypeId, documentNumber);
                return personService.existsByDocument(documentTypeId, documentNumber)
                                .map(exists -> ResponseEntity.ok(exists));
        }

        @Operation(summary = "Verificar si existe persona por email")
        @GetMapping("/exists/email/{email}")
        @PreAuthorize("isAuthenticated()")
        public Mono<ResponseEntity<Boolean>> existsByEmail(
                        @Parameter(description = "Email personal") @PathVariable String email) {
                log.info("Verificando existencia de persona con email: {}", email);
                return personService.existsByEmail(email)
                                .map(exists -> ResponseEntity.ok(exists));
        }
}
