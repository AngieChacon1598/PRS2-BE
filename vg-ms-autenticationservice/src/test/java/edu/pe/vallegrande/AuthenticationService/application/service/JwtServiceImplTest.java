package edu.pe.vallegrande.AuthenticationService.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class JwtServiceImplTest {

        private JwtServiceImpl jwtService;
        private ReactiveJwtDecoder jwtDecoder;
        private final UUID userId = UUID.randomUUID();
        private final String username = "testuser";

        @BeforeEach
        void setUp() {
                jwtDecoder = Mockito.mock(ReactiveJwtDecoder.class);
                jwtService = new JwtServiceImpl(jwtDecoder);
        }

        @Test
        void validateToken_Success() {
                Jwt mockJwt = Jwt.withTokenValue("mock-token")
                                .header("alg", "none")
                                .subject(username)
                                .claim("userId", userId.toString())
                                .build();

                when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(mockJwt));

                StepVerifier.create(jwtService.validateToken("mock-token"))
                                .expectNext(true)
                                .verifyComplete();
        }

        @Test
        void extractUsername_Success() {
                Jwt mockJwt = Jwt.withTokenValue("mock-token")
                                .header("alg", "none")
                                .subject(username)
                                .build();

                when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(mockJwt));

                StepVerifier.create(jwtService.extractUsername("mock-token"))
                                .expectNext(username)
                                .verifyComplete();
        }

        @Test
        void extractUserId_Success() {
                Jwt mockJwt = Jwt.withTokenValue("mock-token")
                                .header("alg", "none")
                                .subject(userId.toString())
                                .claim("userId", userId.toString())
                                .build();

                when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(mockJwt));

                StepVerifier.create(jwtService.extractUserId("mock-token"))
                                .expectNext(userId)
                                .verifyComplete();
        }

        @Test
        void isTokenExpired_True() {
                Jwt mockJwt = Jwt.withTokenValue("mock-token")
                                .header("alg", "none")
                                .expiresAt(Instant.now().minusSeconds(10))
                                .build();

                when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(mockJwt));

                StepVerifier.create(jwtService.isTokenExpired("mock-token"))
                                .expectNext(true)
                                .verifyComplete();
        }

        @Test
        void generateAccessToken_ShouldThrowException() {
                StepVerifier.create(jwtService.generateAccessToken(userId, username,
                                UUID.randomUUID(), Collections.emptyList(), Collections.emptyList()))
                                .expectError(UnsupportedOperationException.class)
                                .verify();
        }
}
