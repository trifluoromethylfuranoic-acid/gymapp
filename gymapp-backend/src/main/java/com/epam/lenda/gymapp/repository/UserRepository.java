package com.epam.lenda.gymapp.repository;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.model.User;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface UserRepository extends Repository<Trainee, Long> {

    @Nonnull
    Optional<User> findById(long id);

    @Nonnull
    Optional<User> findByUsername(@Nonnull String username);

    @Nonnull
    User save(@Nonnull User entity);

    void delete(@Nonnull User entity);

    void delete(long id);
}
