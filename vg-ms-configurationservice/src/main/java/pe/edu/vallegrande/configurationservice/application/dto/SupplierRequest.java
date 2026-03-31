package pe.edu.vallegrande.configurationservice.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class SupplierRequest {

    @NotNull(message = "documentTypesId es requerido")
    private Integer documentTypesId;

    @NotBlank(message = "numeroDocumento es requerido")
    @Size(max = 20, message = "numeroDocumento no puede superar 20 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "legalName es requerido")
    @Size(max = 200, message = "legalName no puede superar 200 caracteres")
    private String legalName;

    @Size(max = 200)
    private String tradeName;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    @Email(message = "email no tiene formato válido")
    private String email;

    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String mainContact;

    @Size(max = 50)
    private String companyType;

    private Boolean isStateProvider;

    @Size(max = 50)
    private String classification;

    private UUID municipalityId;
}
