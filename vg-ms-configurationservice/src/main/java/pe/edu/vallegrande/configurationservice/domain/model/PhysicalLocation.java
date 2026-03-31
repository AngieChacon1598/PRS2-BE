package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import io.r2dbc.postgresql.codec.Point;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import pe.edu.vallegrande.configurationservice.infrastructure.json.PointSerializer;
import pe.edu.vallegrande.configurationservice.infrastructure.json.PointDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("physical_locations")
public class PhysicalLocation {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("location_code")
    private String locationCode;
    private String name;
    private String description;
    @Column("location_type")
    private String locationType; // OFFICE, WAREHOUSE, FIELD, VEHICLE, STORAGE, WORKSHOP

    @Column("parent_location_id")
    private UUID parentLocationId;

    private String address;
    private Integer floor;
    private String sector;
    private String reference;
    @Column("gps_coordinates")
    @JsonSerialize(using = PointSerializer.class)
    @JsonDeserialize(using = PointDeserializer.class)
    private Point gpsCoordinates;

    @Column("max_capacity")
    private Integer maxCapacity;
    @Column("area_m2")
    private BigDecimal areaM2;

    @Column("responsible_id")
    private UUID responsibleId;

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

