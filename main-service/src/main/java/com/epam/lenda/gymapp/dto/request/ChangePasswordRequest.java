package com.epam.lenda.gymapp.dto.request;

import com.epam.lenda.gymapp.validation.annotation.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Builder
@Jacksonized
@ToString
public class ChangePasswordRequest {
    @NotNull @ToString.Exclude
    String oldPassword;

    @NotBlank
    @Password
    @ToString.Exclude
    String newPassword;
}
