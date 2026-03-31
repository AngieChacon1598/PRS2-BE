package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PersonPort {
    Mono<Person> save(Person person);
    Mono<Person> findById(UUID id);
    Flux<Person> findAll();
    Flux<Person> findAllByMunicipalCode(UUID municipalCode);
    Flux<Person> findAllActive();
    Flux<Person> findAllActiveByMunicipalCode(UUID municipalCode);
    Flux<Person> findAllInactive();
    Flux<Person> findAllInactiveByMunicipalCode(UUID municipalCode);
    Mono<Person> findByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);
    Mono<Person> findByPersonalEmail(String email);
    Flux<Person> findByNameContaining(String name);
    Mono<Boolean> existsByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);
    Mono<Boolean> existsByPersonalEmail(String email);
    Mono<Boolean> existsByDocumentAndIdNot(Integer documentTypeId, String documentNumber, UUID id);
    Mono<Boolean> existsByEmailAndIdNot(String email, UUID id);
}
