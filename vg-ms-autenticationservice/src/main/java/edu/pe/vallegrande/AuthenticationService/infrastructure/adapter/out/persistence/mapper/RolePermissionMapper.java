package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionLink;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.RolePermission;

public final class RolePermissionMapper {
    private RolePermissionMapper() {
    }

    public static RolePermissionLink toDomain(RolePermission entity) {
        if (entity == null) {
            return null;
        }
        return RolePermissionLink.builder()
                .roleId(entity.getRoleId())
                .permissionId(entity.getPermissionId())
                .createdAt(entity.getCreatedAt())
                .municipalCode(entity.getMunicipalCode())
                .build();
    }

    public static RolePermission toEntity(RolePermissionLink domain) {
        if (domain == null) {
            return null;
        }
        return RolePermission.builder()
                .roleId(domain.getRoleId())
                .permissionId(domain.getPermissionId())
                .createdAt(domain.getCreatedAt())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }
}

