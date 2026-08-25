package com.epam.lenda.gymapp.common.security;

import jakarta.annotation.Nonnull;
import java.util.UUID;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class UserPrincipal {
    @Nonnull
    private UUID id;
    @Nonnull
    private String username;
    @Nonnull
    private Role role;
}
