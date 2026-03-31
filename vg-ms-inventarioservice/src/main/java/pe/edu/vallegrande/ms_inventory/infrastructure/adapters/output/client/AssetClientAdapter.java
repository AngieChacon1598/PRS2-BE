package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pe.edu.vallegrande.ms_inventory.application.dto.AssetDTO;
import pe.edu.vallegrande.ms_inventory.application.ports.out.AssetClientPort;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AssetClientAdapter implements AssetClientPort {

     @Override
     public Flux<AssetDTO> getAssets(UUID municipalityId, UUID areaId, UUID categoryId, UUID locationId) {
          return Flux.empty(); // temporal para que arranque
     }
}