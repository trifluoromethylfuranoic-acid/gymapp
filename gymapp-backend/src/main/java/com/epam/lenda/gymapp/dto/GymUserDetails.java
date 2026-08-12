package com.epam.lenda.gymapp.dto;

import com.epam.lenda.gymapp.model.Role;
import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@AllArgsConstructor
@Builder
public class GymUserDetails implements UserDetails, CredentialsContainer {
    @Nonnull
    private UUID id;
    @Nonnull
    private String username;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private String password;
    private boolean isActive;
    private boolean isLocked;
    @Nonnull
    private Role role;

    @Override
    public @Nonnull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }
}
