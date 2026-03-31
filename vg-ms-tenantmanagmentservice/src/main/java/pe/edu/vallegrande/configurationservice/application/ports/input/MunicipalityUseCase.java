package pe.edu.vallegrande.configurationservice.application.ports.input;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityRegistrationRequestDTO;
import pe.edu.vallegrande.configurationservice.application.dto.MunicipalityDetailResponseDTO;
import pe.edu.vallegrande.configurationservice.application.dto.ValidationResponseDTO;
import java.util.UUID;

public interface MunicipalityUseCase {
    Flux<Municipality> findAll();

    Mono<Municipality> findById(UUID id);

    Mono<MunicipalityDetailResponseDTO> getDetailById(UUID id, String token);

    Mono<Municipality> create(Municipality municipality);

    Mono<Municipality> register(MunicipalityRegistrationRequestDTO request, String token);

    Mono<Municipality> update(UUID id, MunicipalityRegistrationRequestDTO request, String token);

    Mono<Void> delete(UUID id);

    Flux<Municipality> findByMunicipalityType(String municipalityType);

    Flux<Municipality> findByDepartment(String department);

    Flux<Municipality> findByProvince(String province);

    Mono<ValidationResponseDTO> validateTaxId(String ruc, UUID excludeId);

    Mono<ValidationResponseDTO> validateUbigeo(String ubigeo, UUID excludeId);
}
