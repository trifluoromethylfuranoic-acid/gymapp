package com.epam.lenda.gymapp.repository;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseUserRepository<T> extends JpaRepository<T, UUID> {
    @Query("""
            SELECT u FROM #{#entityName} u WHERE u.user.username = :username
            """)
    @Nonnull
    Optional<T> findByUsername(@Nonnull String username);

    @Query("""
            SELECT count(u) > 0 FROM #{#entityName} u WHERE u.user.username = :username
            """)
    boolean existsByUsername(@Nonnull String username);
}
