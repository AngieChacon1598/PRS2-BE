package pe.edu.vallegrande.configurationservice.application.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantOnboardingRequestDto {
    private String adminUsername;
    private String adminPassword;
    private UUID municipalCode;
    private String authorityName;
    private String email;
}
