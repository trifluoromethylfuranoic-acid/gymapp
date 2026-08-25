package com.epam.lenda.gymapp.dto.request;

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
public class AuthenticationRequest {
    private String username;

    @ToString.Exclude
    private String password;
}
