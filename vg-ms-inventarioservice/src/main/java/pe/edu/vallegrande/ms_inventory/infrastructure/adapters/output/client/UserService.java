package pe.edu.vallegrande.ms_inventory.infrastructure.adapters.output.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import pe.edu.vallegrande.ms_inventory.application.dto.*;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class UserService {

     private final WebClient webClient;

     public UserService(WebClient.Builder builder,
               @Value("${services.user.url}") String userServiceUrl) {
          this.webClient = builder.baseUrl(userServiceUrl).build();
     }

     public Flux<UserDTO> getUsers() {
          return webClient.get()
                    .uri("/api/v1/users")
                    .retrieve()
                    .onStatus(status -> status.isError(),
                              response -> response.bodyToMono(String.class)
                                        .map(body -> new RuntimeException("Error externo: " + body)))
                    .bodyToFlux(UserDTO.class)
                    .timeout(Duration.ofSeconds(5))
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2)));
     }
}