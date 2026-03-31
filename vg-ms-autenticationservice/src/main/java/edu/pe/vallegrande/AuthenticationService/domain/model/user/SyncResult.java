package edu.pe.vallegrande.AuthenticationService.domain.model.user;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SyncResult {
    private final int total;
    private final int synced;
    private final List<String> failed;
}
