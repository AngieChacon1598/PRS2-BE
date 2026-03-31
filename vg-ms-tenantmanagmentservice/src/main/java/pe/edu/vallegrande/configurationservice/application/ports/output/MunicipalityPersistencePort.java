package pe.edu.vallegrande.configurationservice.application.ports.output;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import java.util.UUID;

public interface MunicipalityPersistencePort {
    Flux<Municipality> findAll();

    Mono<Municipality> findById(UUID id);

    Mono<Municipality> save(Municipality municipality);

    Mono<Void> deleteById(UUID id);

    Mono<Boolean> existsByUbigeoCode(String ubigeoCode);

    Mono<Boolean> existsByRuc(String ruc);

    Flux<Municipality> findByMunicipalityType(String municipalityType);

    Flux<Municipality> findByDepartment(String department);

    Flux<Municipality> findByProvince(String province);
}
