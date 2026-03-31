package pe.edu.vallegrande.ms_maintenanceService.domain.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ExternalTenantServicePort {
    Mono<String> getUbigeoCodeByMunicipalityId(UUID municipalityId);
}
