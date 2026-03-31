package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Role;

public final class RoleMapper {
    private RoleMapper() {
    }

    public static RoleModel toDomain(Role entity) {
        if (entity == null) {
            return null;
        }
        return RoleModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .municipalCode(entity.getMunicipalCode())
                .build();
    }

    public static Role toEntity(RoleModel domain) {
        if (domain == null) {
            return null;
        }
        return Role.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .isSystem(domain.getIsSystem())
                .active(domain.getActive())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }
}

