package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.role.RoleModel;
import edu.pe.vallegrande.AuthenticationService.domain.model.role.UpsertRoleCommand;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RoleRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RoleResponseDto;

public final class RoleWebMapper {
    private RoleWebMapper() {
    }

    public static UpsertRoleCommand toCommand(RoleRequestDto dto) {
        return UpsertRoleCommand.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .isSystem(dto.getIsSystem())
                .active(dto.getActive())
                .build();
    }

    public static RoleResponseDto toDto(RoleModel role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystem(role.getIsSystem())
                .active(role.getActive())
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .municipalCode(role.getMunicipalCode())
                .build();
    }
}

