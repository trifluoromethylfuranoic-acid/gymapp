package com.epam.lenda.gymapp.actuator.health;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DbLatencyHealthIndicator implements HealthIndicator {
    private final JdbcTemplate jdbcTemplate;
    private static final long THRESHOLD_MS = 500;

    @Override
    public @Nullable Health health() {
        final var start = System.currentTimeMillis();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            final var end = System.currentTimeMillis();
            final var latency = end - start;

            return (latency >= THRESHOLD_MS ? Health.down() : Health.up()).withDetail("latencyMs", latency).build();
        } catch (Throwable e) {
            return Health.down(e).build();
        }
    }
}
