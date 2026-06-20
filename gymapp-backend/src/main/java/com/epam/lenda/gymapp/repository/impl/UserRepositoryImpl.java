package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.UserRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final MapBasedStorage<User> usersData;

    @Nonnull
    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(usersData.getData().get(id));
    }

    @Nonnull
    @Override
    public Optional<User> findByUsername(@Nonnull String username) {
        return usersData.getData().values().stream().filter(u -> Objects.equals(username, u.getUsername())).findAny();
    }

    @Nonnull
    @Override
    public User save(@Nonnull User entity) {
        if (entity.getId() == null) {
            persist(entity);
        } else {
            merge(entity);
        }
        return entity;
    }

    private void persist(@Nonnull User entity) {
        checkEntity(entity);
        checkDuplicateUsername(entity);
        entity.setId(usersData.nextId());
        usersData.getData().put(entity.getId(), entity);
    }

    private void merge(@Nonnull User entity) {
        checkEntity(entity);
        checkDuplicateUsername(entity);
        usersData.getData().put(entity.getId(), entity);
    }

    @Override
    public void delete(@Nonnull User entity) {
        checkEntity(entity);
        usersData.getData().remove(entity.getId());
    }

    @Override
    public void delete(long id) {
        findById(id).ifPresent(this::delete);
    }

    private void checkEntity(@Nonnull User entity) {
        if (entity.getFirstName() == null || entity.getLastName() == null || entity.getUsername() == null || entity.getPassword() == null || entity.getIsActive() == null) {
            throw new IllegalStateException("Non nullable field is null in User entity");
        }
    }

    private void checkDuplicateUsername(@Nonnull User entity) {
        if (usersData.getData().values().stream().anyMatch(u -> !u.getId().equals(
                entity.getId()) && u.getUsername().equals(entity.getUsername()))) {
            throw new IllegalStateException("Duplicate username");
        }
    }
}
