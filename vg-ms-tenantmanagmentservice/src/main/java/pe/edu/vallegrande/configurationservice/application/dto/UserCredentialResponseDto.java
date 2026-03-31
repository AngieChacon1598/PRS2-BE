package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialResponseDto {
    private UUID id;
    private String username;
    private String passwordHash;
    private UUID municipalCode;
    private UUID personId;
}
