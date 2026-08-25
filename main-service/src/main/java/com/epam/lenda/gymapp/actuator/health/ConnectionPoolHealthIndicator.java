package com.epam.lenda.gymapp.actuator.health;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Nullable;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConnectionPoolHealthIndicator implements HealthIndicator {
    private final DataSource dataSource;

    @Override
    public @Nullable Health health() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            final var pool = hikariDataSource.getHikariPoolMXBean();

            final var active = pool.getActiveConnections();
            final var total = hikariDataSource.getMaximumPoolSize();
            final var waiting = pool.getThreadsAwaitingConnection();

            final var builder = (waiting > 0 || active >= total) ? Health.down() : Health.up();

            return builder.withDetail("activeConnections", active).withDetail("maxConnections", total).withDetail(
                    "threadsAwaitingConnection",
                    waiting).build();
        }

        return Health.unknown().withDetail("dataSourceType", dataSource.getClass().getSimpleName()).build();
    }
}
