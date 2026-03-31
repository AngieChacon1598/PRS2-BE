package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para DocumentType.
 * Se usa para transferir datos entre cliente y backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTypeDTO {
    private Integer id;
    private String code;
    private String description;
    private Integer length;
}
