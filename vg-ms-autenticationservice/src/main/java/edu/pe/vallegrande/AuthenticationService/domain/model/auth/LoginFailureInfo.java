package edu.pe.vallegrande.AuthenticationService.domain.model.auth;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginFailureInfo {
    Integer loginAttempts;
    Integer remainingAttempts;
    LocalDateTime blockedUntil;
    String blockReason;
}

