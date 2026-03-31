package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserCreateRequestDto {
    @jakarta.validation.constraints.NotBlank(message = "El username es obligatorio")
    private String username;

    @jakarta.validation.constraints.NotBlank(message = "La contraseña es obligatoria")
    @jakarta.validation.constraints.Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @jakarta.validation.constraints.Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "La contraseña debe tener al menos una mayúscula, una minúscula, un número y un carácter especial (@#$%^&+=!)")
    private String password;

    @jakarta.validation.constraints.NotNull(message = "El personId es obligatorio")
    private UUID personId;

    private String status = "ACTIVE";
    private UUID municipalCode;
}
