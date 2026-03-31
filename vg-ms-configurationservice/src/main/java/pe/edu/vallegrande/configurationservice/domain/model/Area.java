package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("areas")
public class Area {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("area_code")
    // Código único del área (UNIQUE)
    private String areaCode;
    private String name;
    private String description;

    @Column("parent_area_id")
    // Referencia al área padre para construir la jerarquía (null si es raíz)
    private UUID parentAreaId;

    @Column("hierarchical_level")
    private Integer hierarchicalLevel;

    @Column("responsible_id")
    private UUID responsibleId;

    @Column("physical_location")
    private String physicalLocation;

    private String phone;
    private String email;

    @Column("annual_budget")
    private BigDecimal annualBudget;

    private Boolean active;

    @Column("created_by")
    private UUID createdBy;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("updated_by")
    private UUID updatedBy;
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

