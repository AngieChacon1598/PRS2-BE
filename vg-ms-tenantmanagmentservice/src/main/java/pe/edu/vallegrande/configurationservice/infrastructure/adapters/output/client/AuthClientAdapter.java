package pe.edu.vallegrande.configurationservice.infrastructure.adapters.output.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.configurationservice.application.dto.AssignRoleRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.PersonRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserCreateRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserCredentialResponseDto;
import pe.edu.vallegrande.configurationservice.application.ports.output.AuthClientPort;
import pe.edu.vallegrande.configurationservice.application.dto.UserUpdateRequestDto;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClientAdapter implements AuthClientPort {

    private final WebClient authWebClient;

    @Override
    public Mono<String> login(String username, String password) {
        log.info("Inter-service call - System Login for: {}", username);
        return authWebClient.post()
                .uri("/api/v1/auth/login")
                .bodyValue(Map.of("username", username, "password", password))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> "Bearer " + response.get("accessToken"));
    }

    @Override
    public Mono<UUID> getRoleIdByName(String name, String token) {
        log.info("Inter-service call - GET Role: {}. Token starts with: {}",
                name, token != null ? (token.length() > 20 ? token.substring(0, 20) : token) : "null");

        return authWebClient.get()
                .uri("/api/v1/roles/name/{name}", name)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    log.info("Successfully retrieved role ID for: {}", name);
                    return UUID.fromString((String) response.get("id"));
                });
    }

    @Override
    public Mono<UUID> createPerson(PersonRequestDto personRequest, String token) {
        return authWebClient.post()
                .uri("/api/v1/persons")
                .header("Authorization", token)
                .bodyValue(personRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> UUID.fromString((String) response.get("id")));
    }

    @Override
    public Mono<UUID> createUser(UserCreateRequestDto userRequest, String token) {
        return authWebClient.post()
                .uri("/api/v1/users")
                .header("Authorization", token)
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> UUID.fromString((String) response.get("id")));
    }

    @Override
    public Mono<Void> assignRole(UUID userId, UUID roleId, String token) {
        AssignRoleRequestDto request = new AssignRoleRequestDto();
        return authWebClient.post()
                .uri("/api/v1/assignments/users/{userId}/roles/{roleId}", userId, roleId)
                .header("Authorization", token)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<UserCredentialResponseDto> getUserByMunicipalCode(UUID municipalCode, String token) {
        return authWebClient.get()
                .uri("/api/v1/users/municipal/{municipalCode}", municipalCode)
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(UserCredentialResponseDto.class);
    }

    @Override
    public Mono<Void> updateUser(UUID userId, UserUpdateRequestDto userRequest, String token) {
        return authWebClient.put()
                .uri("/api/v1/users/{id}", userId)
                .header("Authorization", token)
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> onboardTenant(pe.edu.vallegrande.configurationservice.application.dto.TenantOnboardingRequestDto onboardingRequest, String token) {
        return authWebClient.post()
                .uri("/api/v1/users/onboarding")
                .header("Authorization", token)
                .bodyValue(onboardingRequest)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
