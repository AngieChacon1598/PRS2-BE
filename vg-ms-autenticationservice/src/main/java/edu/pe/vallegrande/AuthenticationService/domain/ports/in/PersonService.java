package edu.pe.vallegrande.AuthenticationService.domain.ports.in;

import edu.pe.vallegrande.AuthenticationService.domain.model.person.CreatePersonCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.UpdatePersonCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interfaz del servicio para la gestión de personas
 */
public interface PersonService {
    
    /**
     * Crear una nueva persona
     */
    Mono<Person> createPerson(CreatePersonCommand command);
    
    /**
     * Obtener todas las personas
     */
    Flux<Person> getAllPersons(UUID municipalCode);
    
    /**
     * Obtener todas las personas activas
     */
    Flux<Person> getAllActivePersons(UUID municipalCode);
    
    /**
     * Obtener todas las personas inactivas
     */
    Flux<Person> getAllInactivePersons(UUID municipalCode);
    
    /**
     * Obtener persona por ID
     */
    Mono<Person> getPersonById(UUID id);
    
    /**
     * Obtener persona por documento
     */
    Mono<Person> getPersonByDocument(Integer documentTypeId, String documentNumber);
    
    /**
     * Obtener persona por email
     */
    Mono<Person> getPersonByEmail(String email);
    
    /**
     * Buscar personas por nombre
     */
    Flux<Person> searchPersonsByName(String name);
    
    /**
     * Actualizar una persona
     */
    Mono<Person> updatePerson(UUID id, UpdatePersonCommand command);
    
    /**
     * Eliminar una persona (borrado lógico)
     */
    Mono<Person> deletePerson(UUID id);
    
    /**
     * Restaurar una persona eliminada
     */
    Mono<Person> restorePerson(UUID id);
    
    /**
     * Verificar si existe una persona por documento
     */
    Mono<Boolean> existsByDocument(Integer documentTypeId, String documentNumber);
    
    /**
     * Verificar si existe una persona por email
     */
    Mono<Boolean> existsByEmail(String email);
}