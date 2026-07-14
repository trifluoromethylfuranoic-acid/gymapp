package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseUserRepository<User> {
    @Query("""
            SELECT u FROM #{#entityName} u WHERE u.username = :username
            """)
    @Nonnull
    Optional<User> findByUsername(@Nonnull String username);

    @Query("""
            SELECT count(u) > 0 FROM #{#entityName} u WHERE u.username = :username
            """)
    boolean existsByUsername(@Nonnull String username);
}
