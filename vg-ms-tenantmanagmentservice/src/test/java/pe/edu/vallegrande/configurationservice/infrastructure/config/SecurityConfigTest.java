package pe.edu.vallegrande.configurationservice.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.edu.vallegrande.configurationservice.application.ports.input.MunicipalityUseCase;
import pe.edu.vallegrande.configurationservice.infrastructure.adapters.input.rest.MunicipalityController;
import reactor.core.publisher.Flux;

import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = MunicipalityController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.keycloak.issuer-uri=http://localhost:8080/realms/vallegrande",
        "spring.security.oauth2.resourceserver.jwt.firebase.issuer-uri=https://securetoken.google.com/vallegrande-project",
        "spring.security.oauth2.resourceserver.jwt.supabase.issuer-uri=https://uannlnmvkwrfpyimaaby.supabase.co/auth/v1"
})
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MunicipalityUseCase municipalityUseCase;

    @Test
    void shouldAllowListingMunicipalitiesWithoutAuthentication() {
        given(municipalityUseCase.findAll()).willReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/v1/municipalities")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRequireAuthenticationForCreatingMunicipalities() {
        webTestClient.post()
                .uri("/api/v1/municipalities")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}