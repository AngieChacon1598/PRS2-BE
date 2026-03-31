package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.permission.PermissionModel;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.Permission;

public final class PermissionMapper {
    private PermissionMapper() {
    }

    public static PermissionModel toDomain(Permission entity) {
        if (entity == null) {
            return null;
        }
        return PermissionModel.builder()
                .id(entity.getId())
                .module(entity.getModule())
                .action(entity.getAction())
                .resource(entity.getResource())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .status(entity.getStatus())
                .municipalCode(entity.getMunicipalCode())
                .build();
    }

    public static Permission toEntity(PermissionModel domain) {
        if (domain == null) {
            return null;
        }
        return Permission.builder()
                .id(domain.getId())
                .module(domain.getModule())
                .action(domain.getAction())
                .resource(domain.getResource())
                .displayName(domain.getDisplayName())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .createdBy(domain.getCreatedBy())
                .status(domain.getStatus())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }
}

