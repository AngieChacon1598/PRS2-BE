package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para las respuestas de personas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponseDto {

    private UUID id;
    private Integer documentTypeId;
    private String documentNumber;
    private String personType; // N = Natural, J = Jurídica
    private String firstName;
    private String lastName;
    private String fullName; // Nombre completo
    private LocalDate birthDate;
    private Integer age; // Edad
    private String gender;
    private String personalPhone;
    private String workPhone;
    private String personalEmail;
    private String address;
    private UUID municipalCode;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}