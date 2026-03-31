package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import pe.edu.vallegrande.ms_inventory.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class AssetService {

     private final WebClient webClient;

     public AssetService(WebClient.Builder builder, @Value("${services.asset.url}") String assetServiceUrl) {
          this.webClient = builder.baseUrl(assetServiceUrl).build();
     }

     public Flux<AssetDTO> getAssets(UUID municipalityId, UUID areaId, UUID categoryId, UUID locationId) {

          return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                              .path("/api/v1/assets")
                              .queryParam("municipalityId", municipalityId)
                              .queryParamIfPresent("areaId", Optional.ofNullable(areaId))
                              .queryParamIfPresent("categoryId", Optional.ofNullable(categoryId))
                              .queryParamIfPresent("locationId", Optional.ofNullable(locationId))
                              .build())

                    .retrieve()

                    // Manejo de errores HTTP
                    .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                              .map(body -> new RuntimeException("Error cliente: " + body)))
                    .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                              .map(body -> new RuntimeException("Error servidor: " + body)))

                    .bodyToFlux(AssetDTO.class)

                    // Timeout
                    .timeout(Duration.ofSeconds(5))

                    // Retry controlado
                    .retryWhen(
                              Retry.fixedDelay(2, Duration.ofSeconds(2)));
     }
}