package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.configurationservice.domain.model.SystemConfiguration;


import java.util.UUID;

@Repository
public interface SystemConfigurationRepository extends ReactiveCrudRepository<SystemConfiguration, UUID> {
}
