package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleLink;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.UserRole;

public final class UserRoleMapper {
    private UserRoleMapper() {
    }

    public static UserRoleLink toDomain(UserRole entity) {
        if (entity == null) {
            return null;
        }
        return UserRoleLink.builder()
                .userId(entity.getUserId())
                .roleId(entity.getRoleId())
                .assignedBy(entity.getAssignedBy())
                .assignedAt(entity.getAssignedAt())
                .expirationDate(entity.getExpirationDate())
                .active(entity.getActive())
                .municipalCode(entity.getMunicipalCode())
                .build();
    }

    public static UserRole toEntity(UserRoleLink domain) {
        if (domain == null) {
            return null;
        }
        return UserRole.builder()
                .userId(domain.getUserId())
                .roleId(domain.getRoleId())
                .assignedBy(domain.getAssignedBy())
                .assignedAt(domain.getAssignedAt())
                .expirationDate(domain.getExpirationDate())
                .active(domain.getActive())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }
}

