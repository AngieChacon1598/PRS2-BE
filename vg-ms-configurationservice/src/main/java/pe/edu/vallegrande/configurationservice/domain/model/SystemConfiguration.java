package pe.edu.vallegrande.configurationservice.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("system_configurations")
public class SystemConfiguration {

    @Id
    private UUID id;

    @Column("municipality_id")
    private UUID municipalityId;

    @Column("category")
    private String category;

    @Column("key")
    private String key;

    @Column("value")
    private String value;

    @Column("data_type")
    private String dataType; // string, number, boolean, json, date, time

    @Column("description")
    private String description;

    @Column("is_editable")
    private Boolean isEditable;

    @Column("requires_restart")
    private Boolean requiresRestart;

    @Column("is_sensitive")
    private Boolean isSensitive;

    @Column("minimum_value")
    private String minimumValue;

    @Column("maximum_value")
    private String maximumValue;

    @Column("allowed_values")
    private JsonNode allowedValues;

    @Column("validation_pattern")
    private String validationPattern;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private UUID updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}