package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.ms_maintenanceService.domain.model.Maintenance;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalAssetServicePort;
import pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest.dto.ExternalAssetResponse;
import reactor.core.publisher.Mono;

@Component
public class ExternalAssetServiceAdapter implements ExternalAssetServicePort {

    private final WebClient webClient;

    public ExternalAssetServiceAdapter(
            @Value("${PATRIMONIO_SERVICE_URL}") String patrimonioServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(patrimonioServiceUrl)
                .build();
    }

    @Override
    public Mono<Maintenance> fillAssetDetails(Maintenance maintenance) {
        if (maintenance.getAssetId() == null) {
            return Mono.just(maintenance);
        }
        return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getCredentials())
                .cast(String.class)
                .flatMap(token -> webClient.get()
                        .uri("/api/v1/assets/{id}", maintenance.getAssetId())
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(ExternalAssetResponse.class)
                        .map(response -> {
                            maintenance.setAssetCode(response.getAssetCode());
                            maintenance.setAssetDescription(response.getDescription());
                            return maintenance;
                        }))
                .onErrorResume(e -> {
                    // Si falla la comunicación, devolvemos el objeto sin los detalles del bien
                    return Mono.just(maintenance);
                });
    }
}
