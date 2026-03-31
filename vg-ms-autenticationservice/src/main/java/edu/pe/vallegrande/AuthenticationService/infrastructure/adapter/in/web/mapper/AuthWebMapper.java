package edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.mapper;

import edu.pe.vallegrande.AuthenticationService.domain.model.auth.AuthTokens;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginCommand;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.LoginResult;
import edu.pe.vallegrande.AuthenticationService.domain.model.auth.RefreshTokenCommand;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.LoginRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.LoginResponseDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.RefreshTokenRequestDto;
import edu.pe.vallegrande.AuthenticationService.infrastructure.adapter.in.web.dto.TokenResponseDto;

public final class AuthWebMapper {
    private AuthWebMapper() {
    }

    public static LoginCommand toCommand(LoginRequestDto dto) {
        return LoginCommand.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .build();
    }

    public static RefreshTokenCommand toCommand(RefreshTokenRequestDto dto) {
        return RefreshTokenCommand.builder()
                .refreshToken(dto.getRefreshToken())
                .build();
    }

    public static LoginResponseDto toDto(LoginResult result) {
        AuthTokens tokens = result.getTokens();
        return LoginResponseDto.builder()
                .accessToken(tokens != null ? tokens.getAccessToken() : null)
                .refreshToken(tokens != null ? tokens.getRefreshToken() : null)
                .tokenType(tokens != null ? tokens.getTokenType() : null)
                .expiresIn(tokens != null ? tokens.getExpiresIn() : null)
                .userId(result.getUserId())
                .username(result.getUsername())
                .municipalCode(result.getMunicipalCode())
                .status(result.getStatus())
                .roles(result.getRoles())
                .loginTime(result.getLoginTime())
                .requiresPasswordReset(result.getRequiresPasswordReset())
                .build();
    }

    public static TokenResponseDto toDto(AuthTokens tokens) {
        return TokenResponseDto.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .tokenType(tokens.getTokenType())
                .expiresIn(tokens.getExpiresIn())
                .build();
    }
}

