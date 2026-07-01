package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.HasId;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.BaseUserRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public class BaseUserRepositoryImpl<T extends User & HasId> extends BaseRepositoryImpl<T> implements BaseUserRepository<T> {
    public BaseUserRepositoryImpl(MapBasedStorage<T> storage) {
        super(storage);
    }

    @Override
    public @Nonnull Optional<T> findByUsername(@Nonnull String username) {
        return getStorage().getData().values().stream().filter(user -> user.getUsername().equals(username)).findAny();
    }

    @Override
    protected void checkEntity(@NonNull T entity) {
        super.checkEntity(entity);
        if (entity.getUsername() == null || entity.getPassword() == null || entity.getIsActive() == null || entity.getFirstName() == null || entity.getLastName() == null) {
            throw new IllegalStateException("Non nullable field is null in User entity");
        }
    }
}
