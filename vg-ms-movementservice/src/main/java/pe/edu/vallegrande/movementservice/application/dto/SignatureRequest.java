package pe.edu.vallegrande.movementservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignatureRequest {

    @NotNull(message = "signerId es obligatorio")
    private UUID signerId;

    @NotBlank(message = "signatureType es obligatorio")
    @Pattern(regexp = "delivery|reception", message = "signatureType debe ser delivery o reception")
    @Size(max = 20)
    private String signatureType;

    @Size(max = 4000, message = "observations no puede superar 4000 caracteres")
    private String observations;
}
