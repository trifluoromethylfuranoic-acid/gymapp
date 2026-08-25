package com.epam.lenda.gymapp.common.logging;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1000)
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        final var method = request.getMethod();
        final var uri = request.getRequestURI();
        final var msg = "{} {} -> {}";
        log.debug("Requesting {} {}", method, uri);
        try {
            filterChain.doFilter(request, response);

            final var status = response.getStatus();
            if (status >= 500) {
                log.error(msg, method, uri, status);
            } else {
                log.debug(msg, method, uri, status);
            }
        } catch (Throwable e) {
            log.error(msg, method, uri, 500);
            throw e;
        }
    }
}
