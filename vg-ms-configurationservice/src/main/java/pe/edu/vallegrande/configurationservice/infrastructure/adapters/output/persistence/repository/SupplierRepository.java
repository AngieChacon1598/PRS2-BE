package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface SupplierRepository extends ReactiveCrudRepository<Supplier, UUID> {
    Flux<Supplier> findAllByActiveTrueOrderByLegalNameAsc();
    Flux<Supplier> findAllByActiveFalseOrderByLegalNameAsc();
    
    // Buscar por tipo de documento y número de documento
    Mono<Supplier> findByDocumentTypesIdAndNumeroDocumento(Integer documentTypesId, String numeroDocumento);
}
