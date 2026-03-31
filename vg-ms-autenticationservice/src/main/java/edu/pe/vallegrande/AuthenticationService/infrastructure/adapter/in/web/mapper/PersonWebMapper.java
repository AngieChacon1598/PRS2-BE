package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.person.CreatePersonCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.Person;
import edu.pe.vallegrande.AuthenticationService.domain.model.person.UpdatePersonCommand;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PersonRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.PersonResponseDto;

public final class PersonWebMapper {

    private PersonWebMapper() {
    }

    public static CreatePersonCommand toCommand(PersonRequestDto dto) {
        return CreatePersonCommand.builder()
                .documentTypeId(dto.getDocumentTypeId())
                .documentNumber(dto.getDocumentNumber())
                .personType(dto.getPersonType())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .personalPhone(dto.getPersonalPhone())
                .workPhone(dto.getWorkPhone())
                .personalEmail(dto.getPersonalEmail())
                .address(dto.getAddress())
                .build();
    }

    public static UpdatePersonCommand toUpdateCommand(PersonRequestDto dto) {
        return UpdatePersonCommand.builder()
                .documentTypeId(dto.getDocumentTypeId())
                .documentNumber(dto.getDocumentNumber())
                .personType(dto.getPersonType())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .personalPhone(dto.getPersonalPhone())
                .workPhone(dto.getWorkPhone())
                .personalEmail(dto.getPersonalEmail())
                .address(dto.getAddress())
                .build();
    }

    public static PersonResponseDto toDto(Person person) {
        if (person == null) {
            return null;
        }
        return PersonResponseDto.builder()
                .id(person.getId())
                .documentTypeId(person.getDocumentTypeId())
                .documentNumber(person.getDocumentNumber())
                .personType(person.getPersonType())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .fullName(person.getFullName())
                .birthDate(person.getBirthDate())
                .age(person.getAge())
                .gender(person.getGender())
                .personalPhone(person.getPersonalPhone())
                .workPhone(person.getWorkPhone())
                .personalEmail(person.getPersonalEmail())
                .address(person.getAddress())
                .municipalCode(person.getMunicipalCode())
                .status(person.getStatus())
                .createdAt(person.getCreatedAt())
                .updatedAt(person.getUpdatedAt())
                .build();
    }
}
