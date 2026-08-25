package com.epam.lenda.gymapp.controller.rest;

import com.epam.lenda.gymapp.common.security.UserPrincipal;
import com.epam.lenda.gymapp.dto.GymUserDetails;
import com.epam.lenda.gymapp.dto.request.AuthenticationRequest;
import com.epam.lenda.gymapp.dto.request.ChangePasswordRequest;
import com.epam.lenda.gymapp.dto.request.RefreshRequest;
import com.epam.lenda.gymapp.dto.response.LoginResponse;
import com.epam.lenda.gymapp.exception.WrongCredentialsException;
import com.epam.lenda.gymapp.mapper.UserMapper;
import com.epam.lenda.gymapp.model.Role;
import com.epam.lenda.gymapp.service.AccessTokenService;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Operation(summary = "Login", description = "Exchange credentials for an access token and refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Bad credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody @NotNull AuthenticationRequest loginRequest) {
        final var user = authService.requireAuthentication(loginRequest);
        final var accessToken = accessTokenService.encode(user);
        final var refreshToken = refreshTokenService.create(user.getUsername()).second();

        return new LoginResponse(accessToken, refreshToken);
    }

    @Operation(summary = "Refresh", description = "Exchange refresh token for a new pair of tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody @NotNull RefreshRequest refreshRequest) {
        final var tokenPair = refreshTokenService.validateAndRotate(refreshRequest.refreshToken()).orElseThrow(
                WrongCredentialsException::new);
        final var rawRefreshToken = tokenPair.second();
        final var refreshToken = tokenPair.first();
        final var accessToken = accessTokenService.encode(userMapper.toUserDetails(refreshToken.getUser()));

        return new LoginResponse(accessToken, rawRefreshToken);
    }

    @Operation(summary = "Logout", description = "Revoke a refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Invalid token", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody @NotNull RefreshRequest refreshRequest) {
        refreshTokenService.revoke(refreshRequest.refreshToken());
    }

    @Operation(summary = "Change password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "401", description = "Bad Credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid format or invalid values", content = @Content),
    })
    @PatchMapping("/password")
    public LoginResponse changePassword(@AuthenticationPrincipal UserPrincipal user,
                                        @Valid @NotNull @RequestBody ChangePasswordRequest request) {
        authService.updatePassword(user.getUsername(), request);

        final var accessToken = accessTokenService.encode(toUserDetails(user));
        final var refreshToken = refreshTokenService.create(user.getUsername()).second();

        return new LoginResponse(accessToken, refreshToken);
    }

    private GymUserDetails toUserDetails(UserPrincipal principal) {
        return GymUserDetails
                .builder()
                .id(principal.getId())
                .username(principal.getUsername())
                .role(Role.valueOf(principal.getRole().name()))
                .build();
    }
}
