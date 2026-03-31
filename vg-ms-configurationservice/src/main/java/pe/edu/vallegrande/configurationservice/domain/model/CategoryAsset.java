package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table("asset_categories")
public class CategoryAsset {

    @Id
    @Column("id")
    private UUID id;


    @Column("municipality_id")
    private UUID municipalityId;

    @Column("parent_category_id")
    private UUID parentCategoryId;


    @Column("category_code")
    private String categoryCode;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("level")
    @Builder.Default
    private Integer level = 1;


    @Column("accounting_account")
    private String accountingAccount;

    @Column("annual_depreciation")
    private BigDecimal annualDepreciation;

    @Column("useful_life_years")
    private Integer usefulLifeYears;

    @Column("residual_value_pct")
    @Builder.Default
    private BigDecimal residualValuePct = BigDecimal.ZERO;


    @Column("requires_serial")
    @Builder.Default
    private Boolean requiresSerial = false;

    @Column("requires_plate")
    @Builder.Default
    private Boolean requiresPlate = true;

    @Column("is_inventoriable")
    @Builder.Default
    private Boolean isInventoriable = true;


    @Column("active")
    @Builder.Default
    private Boolean active = true;


    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private UUID updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}