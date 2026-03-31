package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("suppliers")
public class Supplier {

    @Id
    private UUID id; // No inicializar con UUID.randomUUID()

    private Integer documentTypesId;
    private String numeroDocumento;
    private String legalName;
    private String tradeName;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String mainContact;
    private String companyType;
    private Boolean isStateProvider;
    private String classification;
    private UUID municipalityId;
    private Boolean active;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private UUID updatedBy;
    private OffsetDateTime updatedAt;
}
