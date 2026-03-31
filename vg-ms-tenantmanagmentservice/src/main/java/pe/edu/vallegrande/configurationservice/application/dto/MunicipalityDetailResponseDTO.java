package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDetailResponseDTO {
    private Municipality municipality;
    private String adminUsername;
    private String adminPasswordHash;
}
