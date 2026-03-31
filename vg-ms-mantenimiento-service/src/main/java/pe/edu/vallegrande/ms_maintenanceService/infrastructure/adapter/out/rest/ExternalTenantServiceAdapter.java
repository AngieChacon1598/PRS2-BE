package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.ms_maintenanceService.domain.port.out.ExternalTenantServicePort;
import pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.rest.dto.MunicipalityDTO;
import reactor.core.publisher.Mono;

@Component
public class ExternalTenantServiceAdapter implements ExternalTenantServicePort {

    private final WebClient webClient;

    public ExternalTenantServiceAdapter(@Value("${TENANT_SERVICE_URL}") String tenantServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(tenantServiceUrl)
                .build();
    }

    @Override
    public Mono<String> getUbigeoCodeByMunicipalityId(UUID municipalityId) {
        return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getCredentials())
                .cast(String.class)
                .flatMap(token -> webClient.get()
                        .uri("/api/v1/municipalities/{id}", municipalityId)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(MunicipalityDTO.class)
                        .map(MunicipalityDTO::getUbigeoCode))
                .onErrorResume(e -> {
                   // Fallback a un valor por defecto o lanzar error controlado
                   return Mono.just("000000"); 
                });
    }
}
