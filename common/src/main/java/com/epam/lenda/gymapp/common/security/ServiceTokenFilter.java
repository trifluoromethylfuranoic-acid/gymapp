package com.epam.lenda.gymapp.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class ServiceTokenFilter extends OncePerRequestFilter {

    public static final String CALLING_SERVICE_ATTRIBUTE = "callingService";

    private final JwtDecoder jwtDecoder;

    public ServiceTokenFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final var header = request.getHeader("X-Service-Token");
        if (header != null && header.startsWith("Bearer ")) {
            log.trace("X-Service-Token header present");
            try {
                final var jwt = jwtDecoder.decode(header.substring(7));
                if ("service".equals(jwt.getClaimAsString("type"))) {
                    final var clientId = jwt.getClaimAsString("client_id");
                    if (!Strings.isBlank(clientId)) {
                        log.trace("Setting callingService to {}", clientId);
                        request.setAttribute(CALLING_SERVICE_ATTRIBUTE, clientId);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        filterChain.doFilter(request, response);
    }
}
