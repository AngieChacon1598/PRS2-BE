package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pe.edu.vallegrande.configurationservice.domain.model.TestEntity;

public interface TestRepository extends R2dbcRepository<TestEntity, Long> {
}