package edu.pe.vallegrande.AuthenticationService.application.service;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.RefreshTokenCommand;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.ExternalAuthPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthRefreshTokenTest {

    @Mock
    private ExternalAuthPort externalAuthPort;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "defaultRealm", "test-realm");
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "old-refresh-token";
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(refreshToken)
                .build();

        AuthTokens expectedTokens = AuthTokens.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(3600L)
                .build();

        when(externalAuthPort.refreshToken(refreshToken, "test-realm"))
                .thenReturn(Mono.just(expectedTokens));

        StepVerifier.create(authService.refreshToken(command))
                .expectNextMatches(tokens -> tokens.getAccessToken().equals("new-access-token") &&
                        tokens.getRefreshToken().equals("new-refresh-token"))
                .verifyComplete();
    }

    @Test
    void refreshToken_Failure() {
        String refreshToken = "invalid-refresh-token";
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(refreshToken)
                .build();

        when(externalAuthPort.refreshToken(refreshToken, "test-realm"))
                .thenReturn(Mono.error(new RuntimeException("invalid_grant")));

        StepVerifier.create(authService.refreshToken(command))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Error al renovar sesión") &&
                        throwable.getMessage().contains("invalid_grant"))
                .verify();
    }
}
