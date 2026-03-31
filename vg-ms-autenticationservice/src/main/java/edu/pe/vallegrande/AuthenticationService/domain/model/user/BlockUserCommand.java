package edu.pe.vallegrande.AuthenticationService.domain.model.user;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BlockUserCommand {
    String reason;
    LocalDateTime blockedUntil;
    Integer durationHours;
}

