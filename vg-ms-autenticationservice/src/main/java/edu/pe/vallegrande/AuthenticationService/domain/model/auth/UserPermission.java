package edu.pe.vallegrande.AuthenticationService.domain.model.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserPermission {
    String module;
    String action;
    String resource;

    public String asString() {
        return module + ":" + action + ":" + resource;
    }
}

