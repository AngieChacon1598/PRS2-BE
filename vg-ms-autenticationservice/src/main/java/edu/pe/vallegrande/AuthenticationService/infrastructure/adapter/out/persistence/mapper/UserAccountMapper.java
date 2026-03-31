package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.out.persistence.entity.User;

public final class UserAccountMapper {
    private UserAccountMapper() {
    }

    public static UserAccount toDomain(User entity) {
        if (entity == null) {
            return null;
        }
        return UserAccount.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .personId(entity.getPersonId())
                .areaId(entity.getAreaId())
                .positionId(entity.getPositionId())
                .directManagerId(entity.getDirectManagerId())
                .municipalCode(entity.getMunicipalCode())
                .status(entity.getStatus())
                .lastLogin(entity.getLastLogin())
                .loginAttempts(entity.getLoginAttempts())
                .blockedUntil(entity.getBlockedUntil())
                .blockReason(entity.getBlockReason())
                .blockStart(entity.getBlockStart())
                .suspensionReason(entity.getSuspensionReason())
                .suspensionStart(entity.getSuspensionStart())
                .suspensionEnd(entity.getSuspensionEnd())
                .suspendedBy(entity.getSuspendedBy())
                .preferences(entity.getPreferences())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .passwordLastChanged(entity.getPasswordLastChanged())
                .requiresPasswordReset(entity.getRequiresPasswordReset())
                .keycloakId(entity.getKeycloakId())
                .version(entity.getVersion())
                .build();
    }

    public static User toEntity(UserAccount domain) {
        if (domain == null) {
            return null;
        }
        return User.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .passwordHash(domain.getPasswordHash())
                .personId(domain.getPersonId())
                .areaId(domain.getAreaId())
                .positionId(domain.getPositionId())
                .directManagerId(domain.getDirectManagerId())
                .municipalCode(domain.getMunicipalCode())
                .status(domain.getStatus())
                .lastLogin(domain.getLastLogin())
                .loginAttempts(domain.getLoginAttempts())
                .blockedUntil(domain.getBlockedUntil())
                .blockReason(domain.getBlockReason())
                .blockStart(domain.getBlockStart())
                .suspensionReason(domain.getSuspensionReason())
                .suspensionStart(domain.getSuspensionStart())
                .suspensionEnd(domain.getSuspensionEnd())
                .suspendedBy(domain.getSuspendedBy())
                .preferences(domain.getPreferences())
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .updatedBy(domain.getUpdatedBy())
                .updatedAt(domain.getUpdatedAt())
                .passwordLastChanged(domain.getPasswordLastChanged())
                .requiresPasswordReset(domain.getRequiresPasswordReset())
                .keycloakId(domain.getKeycloakId())
                .version(domain.getVersion())
                .build();
    }
}

