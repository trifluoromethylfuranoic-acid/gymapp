package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken t set t.revoked = true where t.user.username = :username
            """)
    void revokeByUsername(String username);

    @Modifying
    @Query("""
            delete from RefreshToken t where t.user.username = :username
            """)
    void deleteByUsername(String username);
}
