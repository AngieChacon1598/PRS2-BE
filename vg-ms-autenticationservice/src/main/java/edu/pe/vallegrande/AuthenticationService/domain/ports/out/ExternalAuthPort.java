package edu.pe.vallegrande.AuthenticationService.domain.ports.out;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import reactor.core.publisher.Mono;

public interface ExternalAuthPort {
    Mono<AuthTokens> login(String username, String password, String realm);
    Mono<AuthTokens> refreshToken(String refreshToken, String realm);
    Mono<Void> logout(String refreshToken, String realm);
    Mono<String> createUser(String username, String password, String realm);
    Mono<String> createUser(String username, String password, String realm, java.util.Map<String, String> attributes);
    Mono<Void> assignRole(String keycloakUserId, String roleName, String realm);
    Mono<Void> updatePassword(String keycloakUserId, String newPassword, String realm);
    Mono<Void> syncRoles(String keycloakUserId, java.util.List<String> roleNames, String realm);
}
