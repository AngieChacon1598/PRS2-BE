package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionTenantDTO {

    private UUID id;
    private UUID municipalityId;
    private String systemName;
    private String logoUrl;
    private String themeColors;
    private String reportsConfiguration;
    private String businessParameters;
    private String defaultCurrency;
    private String timezone;
    private Boolean active;
}
