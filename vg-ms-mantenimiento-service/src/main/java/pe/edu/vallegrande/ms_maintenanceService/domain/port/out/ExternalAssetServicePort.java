package pe.edu.vallegrande.ms_maintenanceService.domain.port.out;

import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import reactor.core.publisher.Mono;

public interface ExternalAssetServicePort {
    Mono<Maintenance> fillAssetDetails(Maintenance maintenance);
}
