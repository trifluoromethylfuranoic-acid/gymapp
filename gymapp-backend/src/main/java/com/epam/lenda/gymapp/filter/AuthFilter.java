package com.epam.lenda.gymapp.filter;

import com.epam.lenda.gymapp.service.AccessTokenService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {
    private final AccessTokenService accessTokenService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        final var header = request.getHeader("Authorization");

        log.debug("Attempting to log in user...");

        if (header != null && header.startsWith("Bearer ")) {
            final var token = header.substring(7);

            log.debug("Authorization header present");

            accessTokenService.parse(token).ifPresent(user -> {
                log.debug("Login successful for user: {}", user.getUsername());

                final var auth = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        } else {
            log.debug("Authorization header absent");
        }

        filterChain.doFilter(request, response);
    }
}
