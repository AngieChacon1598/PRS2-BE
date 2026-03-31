package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import pe.edu.vallegrande.ms_inventory.application.dto.AreaDTO;
import pe.edu.vallegrande.ms_inventory.application.dto.CategoryDTO;
import pe.edu.vallegrande.ms_inventory.application.dto.LocationDTO;
import pe.edu.vallegrande.ms_inventory.application.ports.out.ConfigurationClientPort;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ConfigurationClientAdapter implements ConfigurationClientPort {

     private final ConfigurationService configurationService;

     @Override
     public Flux<AreaDTO> getAreas() {
          return configurationService.getAreas();
     }

     @Override
     public Flux<CategoryDTO> getCategories() {
          return configurationService.getCategories();
     }

     @Override
     public Flux<LocationDTO> getLocations() {
          return configurationService.getLocations();
     }
}