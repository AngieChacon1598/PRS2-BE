package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.repository;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Person;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Repositorio reactivo para la gestión de personas
 */
@Repository
public interface PersonRepository extends R2dbcRepository<Person, UUID> {
    
    /**
     * Buscar persona por tipo y número de documento
     */
    Mono<Person> findByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);
    
    /**
     * Buscar personas por tipo de documento
     */
    Flux<Person> findByDocumentTypeId(Integer documentTypeId);
    
    /**
     * Buscar personas por nombre
     */
    @Query("SELECT * FROM persons WHERE LOWER(first_name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(last_name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Flux<Person> findByNameContaining(String name);
    
    /**
     * Buscar personas por email personal
     */
    Mono<Person> findByPersonalEmail(String email);
    
    /**
     * Buscar personas por género
     */
    Flux<Person> findByGender(String gender);
    
    /**
     * Buscar personas por rango de edad (fecha de nacimiento)
     */
    @Query("SELECT * FROM persons WHERE birth_date BETWEEN :startDate AND :endDate")
    Flux<Person> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Buscar personas nacidas en un año específico
     */
    @Query("SELECT * FROM persons WHERE EXTRACT(YEAR FROM birth_date) = :year")
    Flux<Person> findByBirthYear(Integer year);
    
    /**
     * Verificar si existe una persona con el documento dado
     */
    Mono<Boolean> existsByDocumentTypeIdAndDocumentNumber(Integer documentTypeId, String documentNumber);
    
    /**
     * Verificar si existe una persona con el documento dado excluyendo un ID específico
     */
    @Query("SELECT COUNT(*) > 0 FROM persons WHERE document_type_id = :documentTypeId AND document_number = :documentNumber AND id != :id")
    Mono<Boolean> existsByDocumentAndIdNot(Integer documentTypeId, String documentNumber, UUID id);
    
    /**
     * Verificar si existe una persona con el email dado
     */
    Mono<Boolean> existsByPersonalEmail(String email);
    
    /**
     * Verificar si existe una persona con el email dado excluyendo un ID específico
     */
    @Query("SELECT COUNT(*) > 0 FROM persons WHERE personal_email = :email AND id != :id")
    Mono<Boolean> existsByEmailAndIdNot(String email, UUID id);
    
    /**
     * Contar personas por género
     */
    @Query("SELECT COUNT(*) FROM persons WHERE gender = :gender")
    Mono<Long> countByGender(String gender);
    
    /**
     * Obtener estadísticas de edad
     */
    @Query("SELECT AVG(EXTRACT(YEAR FROM CURRENT_DATE) - EXTRACT(YEAR FROM birth_date)) FROM persons WHERE birth_date IS NOT NULL")
    Mono<Double> getAverageAge();
    
    /**
     * Buscar todas las personas activas
     */
    @Query("SELECT * FROM persons WHERE status = true")
    Flux<Person> findAllActive();
    
    /**
     * Buscar todas las personas inactivas
     */
    @Query("SELECT * FROM persons WHERE status = false")
    Flux<Person> findAllInactive();
    
    /**
     * Buscar personas por estado
     */
    Flux<Person> findByStatus(Boolean status);

    Flux<Person> findByMunicipalCode(UUID municipalCode);

    Flux<Person> findAllByStatusAndMunicipalCode(Boolean status, UUID municipalCode);
}