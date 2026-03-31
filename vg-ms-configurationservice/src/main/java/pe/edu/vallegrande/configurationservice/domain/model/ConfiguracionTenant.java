package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("tenant_configuration")
public class ConfiguracionTenant {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("system_name")
    private String systemName;

    @Column("logo_url")
    private String logoUrl;

    @Column("theme_colors")
    private String themeColors;

    @Column("reports_configuration")
    private String reportsConfiguration;

    @Column("business_parameters")
    private String businessParameters;

    @Column("default_currency")
    private String defaultCurrency;

    @Column("timezone")
    private String timezone;

    @Column("active")
    private Boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
