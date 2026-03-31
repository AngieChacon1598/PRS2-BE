package edu.pe.vallegrande.AuthenticationService.domain.model.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResult {
    AuthTokens tokens;
    UUID userId;
    String username;
    UUID municipalCode;
    String status;
    List<String> roles;
    LocalDateTime loginTime;
    Boolean requiresPasswordReset;
}

