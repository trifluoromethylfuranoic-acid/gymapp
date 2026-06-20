package com.epam.lenda.gymapp.dto.auth;

import com.epam.lenda.gymapp.model.Role;
import java.util.Collection;
import java.util.List;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class UserDetails implements org.springframework.security.core.userdetails.UserDetails, CredentialsContainer {
    private Long id;

    private String username;

    @ToString.Exclude
    private String password;

    private boolean isActive;

    private Role role;

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(getRole());
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive();
    }

    @Override
    public boolean isEnabled() {
        return isActive();
    }
}
