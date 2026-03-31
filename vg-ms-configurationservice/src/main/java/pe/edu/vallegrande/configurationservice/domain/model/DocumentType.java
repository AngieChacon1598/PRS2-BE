package pe.edu.vallegrande.configurationservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("document_types")
public class DocumentType {

    @Id
    @Column("id")
    private Integer id;
    
    @Column("code")
    private String code;
    
    @Column("description")
    private String description;
    
    @Column("length")
    private Integer length;
    
    @Column("active")
    private Boolean active;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
