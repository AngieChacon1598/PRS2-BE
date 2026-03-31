package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence;

import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.PersonPort;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper.PersonPersistenceMapper;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PersonPersistenceAdapter implements PersonPort {

    private final PersonRepository personRepository;

    @Override
    public Mono<Person> save(Person person) {
        return personRepository.save(PersonPersistenceMapper.toEntity(person))
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Mono<Person> findById(UUID id) {
        return personRepository.findById(id)
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Flux<Person> findAll() {
        return personRepository.findAll()
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Flux<Person> findAllByMunicipalCode(UUID municipalCode) {
        return personRepository.findByMunicipalCode(municipalCode)
                .map(PersonPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Person> findAllActive() {
        return personRepository.findAllActive()
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Flux<Person> findAllActiveByMunicipalCode(UUID municipalCode) {
        return personRepository.findAllByStatusAndMunicipalCode(true, municipalCode)
                .map(PersonPersistenceMapper::toDomain);
    }

    @Override
    public Flux<Person> findAllInactive() {
        return personRepository.findAllInactive()
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Flux<Person> findAllInactiveByMunicipalCode(UUID municipalCode) {
        return personRepository.findAllByStatusAndMunicipalCode(false, municipalCode)
                .map(PersonPersistenceMapper::toDomain);
    }

    @Override
    public Mono<Person> findByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber) {
        return personRepository.findByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber)
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Mono<Person> findByPersonalEmail(String email) {
        return personRepository.findByPersonalEmail(email)
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Flux<Person> findByNameContaining(String name) {
        return personRepository.findByNameContaining(name)
                .map(entity -> PersonPersistenceMapper.toDomain(entity));
    }

    @Override
    public Mono<Boolean> existsByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber) {
        return personRepository.existsByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber);
    }

    @Override
    public Mono<Boolean> existsByPersonalEmail(String email) {
        return personRepository.existsByPersonalEmail(email);
    }

    @Override
    public Mono<Boolean> existsByDocumentAndIdNot(Integer documentTypeId, String documentNumber, UUID id) {
        return personRepository.existsByDocumentAndIdNot(documentTypeId, documentNumber, id);
    }

    @Override
    public Mono<Boolean> existsByEmailAndIdNot(String email, UUID id) {
        return personRepository.existsByEmailAndIdNot(email, id);
    }
}
