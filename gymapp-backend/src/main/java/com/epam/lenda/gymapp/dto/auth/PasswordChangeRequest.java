package com.epam.lenda.gymapp.dto.auth;


import com.epam.lenda.gymapp.validation.annotation.Password;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record PasswordChangeRequest(
                                    @NotBlank String oldPassword,
                                    @NotBlank @Password String newPassword
) {
    @Override
    @Nonnull
    public String toString() {
        return "PasswordChangeRequest[]";
    }
}
