package com.epam.lenda.gymapp.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SignupResponse(
                             @NotBlank String username,
                             @NotBlank String password,
                             @NotBlank String accessToken
) {
    @Override
    public String toString() {
        return "SignupResponse[" + "username='" + username + '\'' + ']';
    }
}
