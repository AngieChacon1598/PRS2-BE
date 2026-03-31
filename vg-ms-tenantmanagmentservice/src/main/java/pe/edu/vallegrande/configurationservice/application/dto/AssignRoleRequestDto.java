package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AssignRoleRequestDto {
    private LocalDate expirationDate;
    private Boolean active = true;
}
