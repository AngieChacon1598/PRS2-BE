package pe.edu.vallegrande.ms_maintenanceService.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("maintenance_parts")
public class MaintenancePartEntity {

    @Id
    private UUID id;

    @Column("maintenance_id")
    private UUID maintenanceId;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("part_code")
    private String partCode;

    @Column("part_name")
    private String partName;

    @Column("description")
    private String description;

    @Column("part_type")
    private String partType;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("unit_of_measure")
    private String unitOfMeasure;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @ReadOnlyProperty
    @Column("subtotal")
    private BigDecimal subtotal; // GENERATED

    @Column("supplier_id")
    private UUID supplierId;

    @Column("created_at")
    private LocalDateTime createdAt;
}
