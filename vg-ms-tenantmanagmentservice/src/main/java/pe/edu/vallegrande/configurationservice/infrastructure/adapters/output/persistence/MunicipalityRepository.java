package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import java.util.UUID;

public interface MunicipalityRepository extends ReactiveCrudRepository<Municipality, UUID> {
    Flux<Municipality> findByMunicipalityType(String municipalityType);

    Flux<Municipality> findByDepartment(String department);

    Flux<Municipality> findByProvince(String province);

    Mono<Boolean> existsByRuc(String ruc);

    Mono<Boolean> existsByUbigeoCode(String ubigeoCode);
}
