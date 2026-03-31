package pe.edu.vallegrande.configurationservice.application.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PersonRequestDto {
    private Integer documentTypeId;
    private String documentNumber;
    private String personType;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String personalPhone;
    private String personalEmail;
    private String address;
}
