package edu.pe.vallegrande.AuthenticationService.domain.model.user;

import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateUserCommand {
    String username;
    String password;
    UUID personId;
    UUID areaId;
    UUID positionId;
    UUID directManagerId;
    String status;
    Map<String, Object> preferences;
    Boolean requiresPasswordReset;
}

