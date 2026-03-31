package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityDTO {
    private UUID id;
    private String name;
    private String ruc;
    private String ubigeoCode;
    private String municipalityType;
    private String department;
    private String province;
    private String district;
    private String address;
    private String phoneNumber;
    private String mobileNumber;
    private String email;
    private String website;
    private String mayorName;
    private Boolean isActive;
    private String adminUsername;
    private String adminPasswordHash;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
