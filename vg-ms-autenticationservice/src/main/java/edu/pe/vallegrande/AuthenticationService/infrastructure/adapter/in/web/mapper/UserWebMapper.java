package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.user.BlockUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.CreateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.SuspendUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UpdateUserCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.user.UserAccount;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.BlockUserRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SuspendUserRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserCreateRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserResponseDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.UserUpdateRequestDto;

public final class UserWebMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private UserWebMapper() {
    }

    public static CreateUserCommand toCommand(UserCreateRequestDto dto) {
        return CreateUserCommand.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .personId(dto.getPersonId())
                .areaId(dto.getAreaId())
                .positionId(dto.getPositionId())
                .directManagerId(dto.getDirectManagerId())
                .status(dto.getStatus())
                .preferences(dto.getPreferences())
                .requiresPasswordReset(dto.getRequiresPasswordReset())
                .build();
    }

    public static UpdateUserCommand toCommand(UserUpdateRequestDto dto) {
        return UpdateUserCommand.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .personId(dto.getPersonId())
                .areaId(dto.getAreaId())
                .positionId(dto.getPositionId())
                .directManagerId(dto.getDirectManagerId())
                .status(dto.getStatus())
                .preferences(dto.getPreferences())
                .requiresPasswordReset(dto.getRequiresPasswordReset())
                .build();
    }

    public static SuspendUserCommand toCommand(SuspendUserRequestDto dto) {
        return SuspendUserCommand.builder()
                .reason(dto.getReason())
                .suspensionEnd(dto.getSuspensionEnd())
                .build();
    }

    public static BlockUserCommand toCommand(BlockUserRequestDto dto) {
        return BlockUserCommand.builder()
                .reason(dto.getReason())
                .blockedUntil(dto.getBlockedUntil())
                .durationHours(dto.getDurationHours())
                .build();
    }

    public static UserResponseDto toDto(UserAccount user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .personId(user.getPersonId())
                .areaId(user.getAreaId())
                .positionId(user.getPositionId())
                .directManagerId(user.getDirectManagerId())
                .municipalCode(user.getMunicipalCode())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .loginAttempts(user.getLoginAttempts())
                .blockedUntil(user.getBlockedUntil())
                .blockReason(user.getBlockReason())
                .blockStart(user.getBlockStart())
                .suspensionReason(user.getSuspensionReason())
                .suspensionStart(user.getSuspensionStart())
                .suspensionEnd(user.getSuspensionEnd())
                .suspendedBy(user.getSuspendedBy())
                .preferences(convertStringToMap(user.getPreferences()))
                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt())
                .passwordLastChanged(user.getPasswordLastChanged())
                .requiresPasswordReset(user.getRequiresPasswordReset())
                .version(user.getVersion())
                .build();
    }

    public static edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SyncResultDto toSyncDto(edu.pe.vallegrande.AuthenticationService.domain.model.user.SyncResult result) {
        return edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.SyncResultDto.builder()
                .total(result.getTotal())
                .synced(result.getSynced())
                .failed(result.getFailed())
                .build();
    }

    private static Map<String, Object> convertStringToMap(String str) {
        if (str == null || str.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}

