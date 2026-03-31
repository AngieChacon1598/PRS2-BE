package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.configurationservice.application.ports.output.MunicipalityPersistencePort;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MunicipalityPersistenceAdapter implements MunicipalityPersistencePort {

    private final MunicipalityRepository repository;

    @Override
    public Flux<Municipality> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Municipality> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Municipality> save(Municipality municipality) {
        return repository.save(municipality);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsByUbigeoCode(String ubigeoCode) {
        return repository.existsByUbigeoCode(ubigeoCode);
    }

    @Override
    public Mono<Boolean> existsByRuc(String ruc) {
        return repository.existsByRuc(ruc);
    }

    @Override
    public Flux<Municipality> findByMunicipalityType(String municipalityType) {
        return repository.findByMunicipalityType(municipalityType);
    }

    @Override
    public Flux<Municipality> findByDepartment(String department) {
        return repository.findByDepartment(department);
    }

    @Override
    public Flux<Municipality> findByProvince(String province) {
        return repository.findByProvince(province);
    }
}
