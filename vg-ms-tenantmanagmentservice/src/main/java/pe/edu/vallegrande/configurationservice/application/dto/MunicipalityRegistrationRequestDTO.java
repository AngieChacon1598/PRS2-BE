package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;
import pe.edu.vallegrande.configurationservice.domain.model.Municipality;

@Data
public class MunicipalityRegistrationRequestDTO {
    @jakarta.validation.Valid
    @jakarta.validation.constraints.NotNull(message = "Los datos de la municipalidad son obligatorios")
    private Municipality municipality;

    @jakarta.validation.constraints.NotBlank(message = "El username del administrador es obligatorio")
    private String adminUsername;

    @jakarta.validation.constraints.NotBlank(message = "La contraseña del administrador es obligatoria")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!)")
    private String adminPassword;
}
