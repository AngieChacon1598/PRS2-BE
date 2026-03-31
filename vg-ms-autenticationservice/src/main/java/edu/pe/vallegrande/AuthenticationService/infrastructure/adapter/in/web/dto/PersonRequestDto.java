package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;

import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.validation.MinimumAge;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las solicitudes de creación y actualización de personas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequestDto {

    private static final int MIN_DOC_LENGTH = 8;
    private static final int MAX_DOC_LENGTH = 20;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_AGE = 18;
    private static final int MIN_PHONE_LENGTH = 7;
    private static final int MAX_PHONE_LENGTH = 20;
    private static final int MAX_EMAIL_LENGTH = 200;
    private static final int MIN_ADDRESS_LENGTH = 5;
    private static final int MAX_ADDRESS_LENGTH = 500;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Integer documentTypeId;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = MIN_DOC_LENGTH, max = MAX_DOC_LENGTH, message = "El número de documento debe tener entre " + MIN_DOC_LENGTH + " y " + MAX_DOC_LENGTH + " caracteres")
    private String documentNumber;

    @NotBlank(message = "El tipo de persona es obligatorio")
    @Pattern(regexp = "^[NJ]$", message = "El tipo de persona debe ser N (Natural) o J (Jurídica)")
    private String personType; // N = Natural, J = Jurídica

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "El nombre debe tener entre " + MIN_NAME_LENGTH + " y " + MAX_NAME_LENGTH + " caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH, message = "El apellido debe tener entre " + MIN_NAME_LENGTH + " y " + MAX_NAME_LENGTH + " caracteres")
    private String lastName;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @MinimumAge(value = MIN_AGE, message = "Debe ser mayor de edad (mínimo " + MIN_AGE + " años)")
    private LocalDate birthDate;

    @NotBlank(message = "El género es obligatorio")
    @Pattern(regexp = "^[MF]$", message = "El género debe ser M o F")
    private String gender; // M, F

    @NotBlank(message = "El teléfono personal es obligatorio")
    @Size(min = MIN_PHONE_LENGTH, max = MAX_PHONE_LENGTH, message = "El teléfono personal debe tener entre " + MIN_PHONE_LENGTH + " y " + MAX_PHONE_LENGTH + " caracteres")
    private String personalPhone;

    @Size(max = MAX_PHONE_LENGTH, message = "El teléfono de trabajo no puede exceder " + MAX_PHONE_LENGTH + " caracteres")
    private String workPhone;

    @Email(message = "El email debe ser válido")
    @Size(max = MAX_EMAIL_LENGTH, message = "El email no puede exceder " + MAX_EMAIL_LENGTH + " caracteres")
    private String personalEmail;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = MIN_ADDRESS_LENGTH, max = MAX_ADDRESS_LENGTH, message = "La dirección debe tener entre " + MIN_ADDRESS_LENGTH + " y " + MAX_ADDRESS_LENGTH + " caracteres")
    private String address;
}