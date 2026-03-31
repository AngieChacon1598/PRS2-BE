package edu.pe.vallegrande.AuthenticationService.domain.model.user;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OnboardTenantCommand {
    String adminUsername;
    String adminPassword;
    UUID municipalCode;
    String authorityName;
    String email;
}
