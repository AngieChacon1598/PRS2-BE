package pe.edu.vallegrande.configurationservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.configurationservice.domain.model.Position;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence.repository.PositionRepository;
import pe.edu.vallegrande.configurationservice.infrastructure.config.JwtContextHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository repository;
    private final JwtContextHelper jwtContextHelper;

    public Flux<Position> getAllActive() {
        return repository.findAllByActiveTrue();
    }

    public Flux<Position> getAllInactive() {
        return repository.findAllByActiveFalse();
    }

    public Mono<Position> getById(UUID id) {
        return repository.findById(id);
    }

    public Mono<Position> create(Position position) {
        return jwtContextHelper.getMunicipalityId()
                .flatMap(municipalityId -> {
                    position.setMunicipalityId(municipalityId);
                    position.setActive(true);
                    position.setCreatedAt(LocalDateTime.now());
                    return repository.save(position);
                });
    }

    public Mono<Position> update(UUID id, Position position) {
        return repository.findById(id)
                .flatMap(existing -> {
                    if (Boolean.FALSE.equals(existing.getActive())) {
                        return Mono.error(new RuntimeException("Cannot edit an inactive position"));
                    }
                    if (position.getPositionCode() != null) existing.setPositionCode(position.getPositionCode());
                    if (position.getName() != null) existing.setName(position.getName());
                    if (position.getDescription() != null) existing.setDescription(position.getDescription());
                    if (position.getHierarchicalLevel() != null) existing.setHierarchicalLevel(position.getHierarchicalLevel());
                    if (position.getBaseSalary() != null) existing.setBaseSalary(position.getBaseSalary());
                    return repository.save(existing);
                });
    }

    public Mono<Position> softDelete(UUID id) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setActive(false);
                    return repository.save(existing);
                });
    }

    public Mono<Position> restore(UUID id) {
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setActive(true);
                    return repository.save(existing);
                });
    }
}
