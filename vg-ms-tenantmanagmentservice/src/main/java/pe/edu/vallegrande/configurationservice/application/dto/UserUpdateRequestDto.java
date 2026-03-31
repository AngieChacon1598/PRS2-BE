package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String username;
    private String password;
    private UUID personId;
    private UUID areaId;
    private UUID positionId;
    private UUID directManagerId;
    private String status;
    private Map<String, Object> preferences;
}
