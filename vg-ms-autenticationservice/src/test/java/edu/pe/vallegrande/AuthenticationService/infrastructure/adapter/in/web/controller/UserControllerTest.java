package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.controller;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.UserService;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserCreateRequestDto;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.PermissionService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(controllers = UserController.class, properties = {
                "PORT=8080",
                "KEYCLOAK_URL=http://localhost:8080",
                "KEYCLOAK_REALM=test",
                "KEYCLOAK_CLIENT_ID=test-client",
                "JWT_SECRET=v9y$B&E)H@McQfTjWnZr4u7x!A%C*F-JaNdRgUkXp2s5v8y/B?E(G+KbPeShVmYp"
})
public class UserControllerTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private PermissionService permissionService;

        @MockitoBean
        private JwtService jwtService;

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        void crearUsuario_Exitoso() {
                UserCreateRequestDto request = UserCreateRequestDto.builder()
                                .username("newuser")
                                .password("Password123")
                                .personId(UUID.randomUUID())
                                .build();

                UserAccount savedUser = UserAccount.builder()
                                .id(UUID.randomUUID())
                                .username("newuser")
                                .status("ACTIVE")
                                .build();

                when(userService.createUser(any())).thenReturn(Mono.just(savedUser));

                webTestClient.mutateWith(csrf())
                                .post().uri("/api/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .exchange()
                                .expectStatus().isCreated()
                                .expectBody()
                                .jsonPath("$.username").isEqualTo("newuser");
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        void obtenerTodos_Exitoso() {
                UserAccount user = UserAccount.builder()
                                .id(UUID.randomUUID())
                                .username("user1")
                                .status("ACTIVE")
                                .build();

                when(userService.getAllUsers(any(), any(), any(), any(), any())).thenReturn(Flux.just(user));

                webTestClient.get().uri("/api/v1/users")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$[0].username").isEqualTo("user1");
        }

        @Test
        @WithMockUser(roles = "TENANT_ADMIN")
        void obtenerPorId_Exitoso() {
                UUID id = UUID.randomUUID();
                UserAccount user = UserAccount.builder()
                                .id(id)
                                .username("user1")
                                .status("ACTIVE")
                                .build();

                when(userService.getUserById(id)).thenReturn(Mono.just(user));

                webTestClient.get().uri("/api/v1/users/{id}", id)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.username").isEqualTo("user1");
        }
}
