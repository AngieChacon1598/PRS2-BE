package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginResult;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.AuthService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PermissionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = AuthController.class, properties = {
    "PORT=8080",
    "KEYCLOAK_URL=http://localhost:8080",
    "KEYCLOAK_REALM=test",
    "KEYCLOAK_CLIENT_ID=test-client",
    "JWT_SECRET=v9y$B&E)H@McQfTjWnZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVmYp"
})
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @WithMockUser
    void login_Exitoso() {
        LoginCommand command = LoginCommand.builder()
                .username("testuser")
                .password("password123")
                .build();

        LoginResult result = LoginResult.builder()
                .userId(UUID.randomUUID())
                .username("testuser")
                .tokens(AuthTokens.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .build())
                .build();

        when(authService.login(any())).thenReturn(Mono.just(result));

        webTestClient.mutateWith(csrf())
                .post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testuser")
                .jsonPath("$.accessToken").isEqualTo("access-token");
    }

    @Test
    @WithMockUser
    void login_Fallido() {
        LoginCommand command = LoginCommand.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        when(authService.login(any())).thenReturn(Mono.error(new RuntimeException("Credenciales inválidas")));
        // Need to mock getLoginFailureInfo as it's called in onErrorResume
        when(authService.getLoginFailureInfo("testuser")).thenReturn(Mono.empty());

        webTestClient.mutateWith(csrf())
                .post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
