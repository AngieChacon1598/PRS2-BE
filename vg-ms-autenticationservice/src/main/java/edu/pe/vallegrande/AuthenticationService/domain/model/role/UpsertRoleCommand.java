package edu.pe.vallegrande.AuthenticationService.domain.model.role;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UpsertRoleCommand {
    String name;
    String description;
    Boolean isSystem;
    Boolean active;
}

