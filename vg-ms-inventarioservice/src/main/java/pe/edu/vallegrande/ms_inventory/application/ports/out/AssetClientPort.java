package pe.edu.vallegrande.ms_inventory.application.ports.out;

import reactor.core.publisher.Flux;

import java.util.UUID;

import pe.edu.vallegrande.ms_inventory.application.dto.*;

public interface AssetClientPort {

    Flux<AssetDTO> getAssets(UUID municipalityId, UUID areaId, UUID categoryId, UUID locationId);
}
