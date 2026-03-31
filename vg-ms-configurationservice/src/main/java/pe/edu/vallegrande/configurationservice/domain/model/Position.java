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
@Table("positions")
public class Position {

    @Id
    private UUID id; // UUID PRIMARY KEY DEFAULT gen_random_uuid()

    @Column("position_code")
    private String positionCode; // VARCHAR(10) UNIQUE NOT NULL

    @Column("name")
    private String name; // VARCHAR(100) NOT NULL

    @Column("description")
    private String description; // TEXT

    @Column("hierarchical_level")
    private Integer hierarchicalLevel; // INTEGER DEFAULT 1

    @Column("base_salary")
    private BigDecimal baseSalary; // NUMERIC(10,2)

    @Column("active")
    @Builder.Default
    private Boolean active = true; // BOOLEAN DEFAULT true

    @Column("created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // TIMESTAMP DEFAULT now()

    @Column("municipality_id")
    private UUID municipalityId; // UUID
}
