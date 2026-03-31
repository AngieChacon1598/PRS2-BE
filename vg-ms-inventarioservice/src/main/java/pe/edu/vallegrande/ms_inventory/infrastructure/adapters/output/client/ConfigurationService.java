package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import pe.edu.vallegrande.ms_inventory.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class ConfigurationService {

     private final WebClient webClient;

     public ConfigurationService(WebClient.Builder builder,
               @Value("${services.configuration.url}") String configServiceUrl) {
          this.webClient = builder.baseUrl(configServiceUrl).build();
     }

     public Flux<AreaDTO> getAreas() {
          return webClient.get()
                    .uri("/api/v1/areas")
                    .retrieve()
                    .onStatus(status -> status.isError(),
                              response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Error externo: " + body)))
                    .bodyToFlux(AreaDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)));
     }

     public Flux<CategoryDTO> getCategories() {
          return webClient.get()
                    .uri("/api/v1/categories-assets")
                    .retrieve()
                    .onStatus(status -> status.isError(),
                              response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Error externo: " + body)))
                    .bodyToFlux(CategoryDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)));
     }

     public Flux<LocationDTO> getLocations() {
          return webClient.get()
                    .uri("/api/v1/physical-locations")
                    .retrieve()
                    .onStatus(status -> status.isError(),
                              response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Error externo: " + body)))
                    .bodyToFlux(LocationDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)));
     }
}