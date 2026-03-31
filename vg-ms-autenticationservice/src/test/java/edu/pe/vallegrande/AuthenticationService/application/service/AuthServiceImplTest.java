package edu.pe.vallegrande.AuthenticationService.application.service;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.domain.ports.in.JwtService;
import edu.pe.vallegrande.AuthenticationService.domain.ports.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

        @Mock
        private AuthUserPort authUserPort;
        @Mock
        private AuthPermissionPort authPermissionPort;
        @Mock
        private JwtService jwtService;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private TokenBlacklistPort tokenBlacklistPort;
        @Mock
        private ExternalAuthPort externalAuthPort;
        @Mock
        private CachePort cachePort;

        @InjectMocks
        private AuthServiceImpl authService;

        private UserAccount testUser;
        private final UUID userId = UUID.randomUUID();
        private final String username = "testuser";
        private final String password = "password123";

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(authService, "defaultRealm", "testrealm");
                testUser = UserAccount.builder()
                                .id(userId)
                                .username(username)
                                .passwordHash("hashedPassword")
                                .status("ACTIVE")
                                .municipalCode(UUID.randomUUID())
                                .build();
        }

        @Test
        void login_Success() {
                LoginCommand command = LoginCommand.builder()
                                .username(username)
                                .password(password)
                                .build();

                AuthTokens tokens = AuthTokens.builder()
                                .accessToken("access-token")
                                .refreshToken("refresh-token")
                                .build();

                when(authUserPort.findByUsername(username)).thenReturn(Mono.just(testUser));
                when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);
                when(authUserPort.updateLastLogin(eq(userId), any())).thenReturn(Mono.empty());
                when(authUserPort.findActiveRoleNames(userId)).thenReturn(Flux.just("ROLE_USER"));
                when(externalAuthPort.login(username, password, "testrealm")).thenReturn(Mono.just(tokens));

                StepVerifier.create(authService.login(command))
                                .expectNextMatches(result -> result.getUsername().equals(username) &&
                                                result.getTokens().getAccessToken().equals("access-token"))
                                .verifyComplete();

                verify(authUserPort).updateLastLogin(eq(userId), any());
        }

        @Test
        void login_InvalidCredentials() {
                LoginCommand command = LoginCommand.builder()
                                .username(username)
                                .password("wrongpassword")
                                .build();

                when(authUserPort.findByUsername(username)).thenReturn(Mono.just(testUser));
                when(passwordEncoder.matches("wrongpassword", testUser.getPasswordHash())).thenReturn(false);
                when(authUserPort.incrementLoginAttempts(userId)).thenReturn(Mono.empty());
                when(authUserPort.findById(userId)).thenReturn(Mono.just(testUser));

                StepVerifier.create(authService.login(command))
                                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                                                throwable.getMessage().equals("Credenciales inválidas"))
                                .verify();
        }

        @Test
        void validateToken_Success() {
                String token = "valid-token";
                when(tokenBlacklistPort.isBlacklisted(token)).thenReturn(Mono.just(false));
                when(jwtService.validateToken(token)).thenReturn(Mono.just(true));

                StepVerifier.create(authService.validateToken(token))
                                .expectNext(true)
                                .verifyComplete();
        }

        @Test
        void validateToken_Blacklisted() {
                String token = "blacklisted-token";
                when(tokenBlacklistPort.isBlacklisted(token)).thenReturn(Mono.just(true));

                StepVerifier.create(authService.validateToken(token))
                                .expectNext(false)
                                .verifyComplete();

                verify(jwtService, never()).validateToken(anyString());
        }
}
