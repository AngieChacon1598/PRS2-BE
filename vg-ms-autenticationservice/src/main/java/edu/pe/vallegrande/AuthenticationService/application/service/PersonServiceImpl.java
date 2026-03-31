package edu.pe.vallegrande.AuthenticationService.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import edu.pe.vallegrande.AuthenticationService.domain.exception.DuplicateResourceException;
import edu.pe.vallegrande.AuthenticationService.domain.exception.ResourceNotFoundException;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.CreatePersonCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.UpdatePersonCommand;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PersonService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.CurrentUserPort;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PersonPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementación del servicio de gestión de personas */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonPort personPort;
    private final CurrentUserPort currentUserPort;

    @Override
    public Mono<Person> createPerson(CreatePersonCommand command) {
        log.info("Creando nueva persona: {} {}", command.getFirstName(), command.getLastName());

        return personPort.existsByDocumentTypeIdAndDocumentNumber(
                command.getDocumentTypeId(),
                command.getDocumentNumber())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException(
                                "Ya existe una persona con el documento: " + command.getDocumentNumber()));
                    }

                    // Verificar email si se proporciona
                    if (command.getPersonalEmail() != null
                            && !command.getPersonalEmail().trim().isEmpty()) {
                        return personPort.existsByPersonalEmail(command.getPersonalEmail())
                                .flatMap(emailExists -> {
                                    if (emailExists) {
                                        return Mono.error(new DuplicateResourceException(
                                                "Ya existe una persona con el email: "
                                                        + command.getPersonalEmail()));
                                    }
                                    return buildNewPerson(command);
                                });
                    }

                    return buildNewPerson(command);
                })
                .flatMap(personPort::save)
                .doOnSuccess(person -> log.info("Persona creada exitosamente: {}", person.getFullName()))
                .doOnError(error -> log.error("Error al crear persona: {}", error.getMessage()));
    }

    @Override
    public Flux<Person> getAllPersons(UUID municipalCode) {
        log.info("Obteniendo todas las personas para municipalidad: {}", municipalCode);
        if (municipalCode != null) {
            return personPort.findAllByMunicipalCode(municipalCode);
        }
        return personPort.findAll();
    }

    @Override
    public Flux<Person> getAllActivePersons(UUID municipalCode) {
        log.info("Obteniendo todas las personas activas para municipalidad: {}", municipalCode);
        if (municipalCode != null) {
            return personPort.findAllActiveByMunicipalCode(municipalCode);
        }
        return personPort.findAllActive();
    }

    @Override
    public Flux<Person> getAllInactivePersons(UUID municipalCode) {
        log.info("Obteniendo todas las personas inactivas para municipalidad: {}", municipalCode);
        if (municipalCode != null) {
            return personPort.findAllInactiveByMunicipalCode(municipalCode);
        }
        return personPort.findAllInactive();
    }

    @Override
    public Mono<Person> getPersonById(UUID id) {
        log.info("Obteniendo persona por ID: {}", id);
        return personPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Persona no encontrada con ID: " + id)));
    }

    @Override
    public Mono<Person> getPersonByDocument(Integer documentTypeId, String documentNumber) {
        log.info("Obteniendo persona por documento: {} - {}", documentTypeId, documentNumber);
        return personPort.findByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Persona no encontrada con documento: " + documentNumber)));
    }

    @Override
    public Mono<Person> getPersonByEmail(String email) {
        log.info("Obteniendo persona por email: {}", email);
        return personPort.findByPersonalEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Persona no encontrada con email: " + email)));
    }

    @Override
    public Flux<Person> searchPersonsByName(String name) {
        log.info("Buscando personas por nombre: {}", name);
        return personPort.findByNameContaining(name);
    }

    @Override
    public Mono<Person> updatePerson(UUID id, UpdatePersonCommand command) {
        log.info("Actualizando persona con ID: {}", id);

        return personPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Persona no encontrada con ID: " + id)))
                .flatMap(existingPerson -> {
                    // Verificar documento si cambió
                    if (!existingPerson.getDocumentTypeId().equals(command.getDocumentTypeId()) ||
                            !existingPerson.getDocumentNumber().equals(command.getDocumentNumber())) {

                        return personPort.existsByDocumentAndIdNot(
                                command.getDocumentTypeId(),
                                command.getDocumentNumber(),
                                id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateResourceException(
                                                "Ya existe una persona con el documento: "
                                                        + command.getDocumentNumber()));
                                    }
                                    return Mono.just(existingPerson);
                                });
                    }
                    return Mono.just(existingPerson);
                })
                .flatMap(existingPerson -> {
                    // Verificar email si cambió
                    if (command.getPersonalEmail() != null &&
                            !command.getPersonalEmail().equals(existingPerson.getPersonalEmail())) {

                        return personPort.existsByEmailAndIdNot(command.getPersonalEmail(), id)
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new DuplicateResourceException(
                                                "Ya existe una persona con el email: "
                                                        + command.getPersonalEmail()));
                                    }
                                    return Mono.just(existingPerson);
                                });
                    }
                    return Mono.just(existingPerson);
                })
                .flatMap(existingPerson -> currentUserPort.currentUserId().map(currentUserId -> {
                    existingPerson.setDocumentTypeId(command.getDocumentTypeId());
                    existingPerson.setDocumentNumber(command.getDocumentNumber());
                    existingPerson.setPersonType(command.getPersonType());
                    existingPerson.setFirstName(command.getFirstName());
                    existingPerson.setLastName(command.getLastName());
                    existingPerson.setBirthDate(command.getBirthDate());
                    existingPerson.setGender(command.getGender());
                    existingPerson.setPersonalPhone(command.getPersonalPhone());
                    existingPerson.setWorkPhone(command.getWorkPhone());
                    existingPerson.setPersonalEmail(command.getPersonalEmail());
                    existingPerson.setAddress(command.getAddress());
                    existingPerson.setUpdatedAt(LocalDateTime.now());
                    existingPerson.setUpdatedBy(currentUserId);
                    return existingPerson;
                }))
                .flatMap(personPort::save);
    }

    @Override
    public Mono<Person> deletePerson(UUID id) {
        log.info("Eliminando persona (borrado lógico) con ID: {}", id);
        return personPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Persona no encontrada con ID: " + id)))
                .flatMap(person -> currentUserPort.currentUserId().flatMap(currentUserId -> {
                    person.setStatus(false);
                    person.setUpdatedAt(LocalDateTime.now());
                    person.setUpdatedBy(currentUserId);
                    return personPort.save(person);
                }))
                .doOnSuccess(p -> log.info("Persona eliminada (status=false) con ID: {}", id));
    }

    @Override
    public Mono<Person> restorePerson(UUID id) {
        log.info("Restaurando persona con ID: {}", id);
        return personPort.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Persona no encontrada con ID: " + id)))
                .flatMap(person -> currentUserPort.currentUserId().flatMap(currentUserId -> {
                    person.setStatus(true);
                    person.setUpdatedAt(LocalDateTime.now());
                    person.setUpdatedBy(currentUserId);
                    return personPort.save(person);
                }))
                .doOnSuccess(p -> log.info("Persona restaurada (status=true) con ID: {}", id));
    }

    @Override
    public Mono<Boolean> existsByDocument(Integer documentTypeId, String documentNumber) {
        return personPort.existsByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return personPort.existsByPersonalEmail(email);
    }

    /**
     * Crear instancia de Person desde Command
     */
    private Mono<Person> buildNewPerson(CreatePersonCommand command) {
        return Mono.zip(
                currentUserPort.currentMunicipalCode()
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty()),
                currentUserPort.currentUserId()
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty()))
                .map(tuple -> Person.builder()
                        .documentTypeId(command.getDocumentTypeId())
                        .documentNumber(command.getDocumentNumber())
                        .personType(command.getPersonType())
                        .firstName(command.getFirstName())
                        .lastName(command.getLastName())
                        .birthDate(command.getBirthDate())
                        .gender(command.getGender())
                        .personalPhone(command.getPersonalPhone())
                        .workPhone(command.getWorkPhone())
                        .personalEmail(command.getPersonalEmail())
                        .address(command.getAddress())
                        .municipalCode(tuple.getT1().orElse(null))
                        .status(true)
                        .createdAt(LocalDateTime.now())
                        .createdBy(tuple.getT2().orElse(null))
                        .updatedAt(LocalDateTime.now())
                        .updatedBy(tuple.getT2().orElse(null))
                        .build());
    }
}