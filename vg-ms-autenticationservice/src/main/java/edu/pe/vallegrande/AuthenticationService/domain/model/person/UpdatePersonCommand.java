package edu.pe.vallegrande.AuthenticationService.domain.model.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonCommand {
    private UUID id;
    private Integer documentTypeId;
    private String documentNumber;
    private String personType;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String personalPhone;
    private String workPhone;
    private String personalEmail;
    private String address;
}
