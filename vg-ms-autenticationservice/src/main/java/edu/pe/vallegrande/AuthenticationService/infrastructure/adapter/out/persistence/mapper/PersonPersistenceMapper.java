package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

public final class PersonPersistenceMapper {
    private PersonPersistenceMapper() {
    }

    public static edu.pe.vallegrande.AuthenticationService.domain.model.person.Person toDomain(edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Person entity) {
        if (entity == null) {
            return null;
        }
        return edu.pe.vallegrande.AuthenticationService.domain.model.person.Person.builder()
                .id(entity.getId())
                .documentTypeId(entity.getDocumentTypeId())
                .documentNumber(entity.getDocumentNumber())
                .personType(entity.getPersonType())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .birthDate(entity.getBirthDate())
                .gender(entity.getGender())
                .personalPhone(entity.getPersonalPhone())
                .workPhone(entity.getWorkPhone())
                .personalEmail(entity.getPersonalEmail())
                .address(entity.getAddress())
                .municipalCode(entity.getMunicipalCode())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Person toEntity(edu.pe.vallegrande.AuthenticationService.domain.model.person.Person domain) {
        if (domain == null) {
            return null;
        }
        return edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Person.builder()
                .id(domain.getId())
                .documentTypeId(domain.getDocumentTypeId())
                .documentNumber(domain.getDocumentNumber())
                .personType(domain.getPersonType())
                .firstName(domain.getFirstName())
                .lastName(domain.getLastName())
                .birthDate(domain.getBirthDate())
                .gender(domain.getGender())
                .personalPhone(domain.getPersonalPhone())
                .workPhone(domain.getWorkPhone())
                .personalEmail(domain.getPersonalEmail())
                .address(domain.getAddress())
                .municipalCode(domain.getMunicipalCode())
                .status(domain.getStatus())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedBy(domain.getUpdatedBy())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
