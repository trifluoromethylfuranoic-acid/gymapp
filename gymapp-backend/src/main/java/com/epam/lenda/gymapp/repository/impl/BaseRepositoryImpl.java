package com.epam.lenda.gymapp.repository.impl;

import com.epam.lenda.gymapp.model.HasId;
import com.epam.lenda.gymapp.repository.BaseRepository;
import com.epam.lenda.gymapp.util.MapBasedStorage;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor
public abstract class BaseRepositoryImpl<T extends HasId> implements BaseRepository<T> {
    private final MapBasedStorage<T> storage;

    @Nonnull
    protected MapBasedStorage<T> getStorage() {
        return storage;
    }

    @Override
    public @NonNull List<T> findAll() {
        return storage.getData().values().stream().toList();
    }

    @Override
    public @NonNull Optional<T> findById(long id) {
        return Optional.ofNullable(storage.getData().get(id));
    }

    @Override
    public @NonNull T save(@NonNull T entity) {
        checkEntity(entity);
        saveCascade(entity);
        storage.save(entity);
        return entity;
    }

    @Override
    public void delete(T entity) {
        deleteCascade(entity);
        storage.getData().remove(entity.getId());
    }

    protected void deleteCascade(T entity) {
    }

    protected void saveCascade(@Nonnull T entity) {
    }

    protected void checkEntity(@Nonnull T entity) {
    }
}
