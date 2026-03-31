package edu.pe.vallegrande.AuthenticationService.domain.model.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginCommand {
    String username;
    String password;
}

