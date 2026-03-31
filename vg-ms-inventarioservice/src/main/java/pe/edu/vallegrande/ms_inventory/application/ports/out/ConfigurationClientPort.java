package pe.edu.vallegrande.ms_inventory.application.ports.out;

import pe.edu.vallegrande.ms_inventory.application.dto.*;
import reactor.core.publisher.Flux;

public interface ConfigurationClientPort {

    Flux<AreaDTO> getAreas();

    Flux<CategoryDTO> getCategories();

    Flux<LocationDTO> getLocations();
}
