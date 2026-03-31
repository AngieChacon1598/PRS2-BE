package pe.edu.vallegrande.configurationservice.application.ports.output;

import pe.edu.vallegrande.configurationservice.application.dto.PersonRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserCreateRequestDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserCredentialResponseDto;
import pe.edu.vallegrande.configurationservice.application.dto.UserUpdateRequestDto;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface AuthClientPort {
    Mono<String> login(String username, String password);

    Mono<UUID> getRoleIdByName(String name, String token);

    Mono<UUID> createPerson(PersonRequestDto personRequest, String token);

    Mono<UUID> createUser(UserCreateRequestDto userRequest, String token);

    Mono<Void> assignRole(UUID userId, UUID roleId, String token);

    Mono<UserCredentialResponseDto> getUserByMunicipalCode(UUID municipalCode, String token);

    Mono<Void> updateUser(UUID userId, UserUpdateRequestDto userRequest, String token);

    Mono<Void> onboardTenant(pe.edu.vallegrande.configurationservice.application.dto.TenantOnboardingRequestDto onboardingRequest, String token);
}
