package edu.pe.vallegrande.AuthenticationService.domain.model.assignment;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AssignRoleCommand {
    LocalDate expirationDate;
    Boolean active;
}

