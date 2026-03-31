package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.DocumentType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentTypeRepository extends ReactiveCrudRepository<DocumentType, Integer> {

    @Query("SELECT * FROM document_types WHERE active = TRUE")
    Flux<DocumentType> findAllActive();

    @Query("SELECT * FROM document_types WHERE active = FALSE")
    Flux<DocumentType> findAllInactive();

    @Query("""
        UPDATE document_types 
        SET code = :code, 
            description = :description, 
            length = :length, 
            active = :active, 
            updated_at = NOW() 
        WHERE id = :id
    """)
    Mono<Integer> updateDocumentType(Integer id, String code, String description, Integer length, Boolean active);
}