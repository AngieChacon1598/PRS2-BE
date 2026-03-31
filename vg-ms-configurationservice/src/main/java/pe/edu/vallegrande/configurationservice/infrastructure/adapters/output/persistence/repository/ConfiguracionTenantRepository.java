package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.ConfiguracionTenant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ConfiguracionTenantRepository extends R2dbcRepository<ConfiguracionTenant, UUID> {
    
    Mono<ConfiguracionTenant> findByMunicipalityId(UUID municipalityId);
    
    Flux<ConfiguracionTenant> findByActive(Boolean active);
}
