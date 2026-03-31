package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.AssignRoleCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.RolePermissionAssignment;
import edu.pe.vallegrande.AuthenticationService.domain.model.assignment.UserRoleAssignment;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.AssignRoleRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RolePermissionAssignmentDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserRoleAssignmentDto;

public final class AssignmentWebMapper {
    private AssignmentWebMapper() {
    }

    public static AssignRoleCommand toCommand(AssignRoleRequestDto dto) {
        return AssignRoleCommand.builder()
                .expirationDate(dto.getExpirationDate())
                .active(dto.getActive())
                .build();
    }

    public static UserRoleAssignmentDto toDto(UserRoleAssignment domain) {
        return UserRoleAssignmentDto.builder()
                .userId(domain.getUserId())
                .username(domain.getUsername())
                .roleId(domain.getRoleId())
                .roleName(domain.getRoleName())
                .roleDescription(domain.getRoleDescription())
                .assignedBy(domain.getAssignedBy())
                .assignedByUsername(domain.getAssignedByUsername())
                .assignedAt(domain.getAssignedAt())
                .expirationDate(domain.getExpirationDate())
                .active(domain.getActive())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }

    public static RolePermissionAssignmentDto toDto(RolePermissionAssignment domain) {
        return RolePermissionAssignmentDto.builder()
                .roleId(domain.getRoleId())
                .roleName(domain.getRoleName())
                .permissionId(domain.getPermissionId())
                .module(domain.getModule())
                .action(domain.getAction())
                .resource(domain.getResource())
                .description(domain.getDescription())
                .createdAt(domain.getCreatedAt())
                .municipalCode(domain.getMunicipalCode())
                .build();
    }
}

