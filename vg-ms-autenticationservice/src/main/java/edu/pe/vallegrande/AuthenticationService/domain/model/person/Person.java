package edu.pe.vallegrande.AuthenticationService.domain.model.person;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {
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
    private UUID municipalCode;
    private Boolean status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private UUID updatedBy;
    private LocalDateTime updatedAt;

    public String getFullName() {
        return (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
    }

    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}
