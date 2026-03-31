package edu.pe.vallegrande.AuthenticationService.domain.model.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthTokens {
    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
}

