package pe.edu.vallegrande.patrimonio_service.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table(value = "asset_disposal_detail")
public class AssetDisposalDetail {
    @Id
    @Column("id")
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("disposal_id")
    private UUID disposalId;

    @Column("asset_id")
    private UUID assetId;

    // Asset evaluation
    @Column("conservation_status")
    private String conservationStatus;

    @Column("book_value")
    private BigDecimal bookValue;

    @Column("recoverable_value")
    private BigDecimal recoverableValue;

    // Technical opinion
    @Column("technical_opinion")
    private String technicalOpinion;

    @Column("recommendation")
    private String recommendation;

    // Disposal execution
    @Column("removal_date")
    private LocalDate removalDate;

    @Column("removal_responsible_id")
    private UUID removalResponsibleId;

    @Column("final_destination")
    private String finalDestination;

    // Documentation
    @Column("observations")
    private String observations;

    @Column("condition_photographs")
    private String conditionPhotographs;

    // Audit
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime lastModifiedAt;
}
