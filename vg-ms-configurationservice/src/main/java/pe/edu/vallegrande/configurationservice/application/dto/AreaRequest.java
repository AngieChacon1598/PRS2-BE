package pe.edu.vallegrande.configurationservice.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AreaRequest {

    @NotNull(message = "municipalityId es requerido")
    private UUID municipalityId;

    @NotBlank(message = "areaCode es requerido")
    @Size(max = 20, message = "areaCode no puede superar 20 caracteres")
    private String areaCode;

    @NotBlank(message = "name es requerido")
    @Size(max = 100, message = "name no puede superar 100 caracteres")
    private String name;

    @Size(max = 255, message = "description no puede superar 255 caracteres")
    private String description;

    private UUID parentAreaId;

    @Min(value = 1, message = "hierarchicalLevel debe ser mayor a 0")
    private Integer hierarchicalLevel;

    private UUID responsibleId;

    @Size(max = 255)
    private String physicalLocation;

    @Size(max = 20)
    private String phone;

    @Email(message = "email no tiene formato válido")
    private String email;

    @DecimalMin(value = "0.0", message = "annualBudget no puede ser negativo")
    private BigDecimal annualBudget;
}
