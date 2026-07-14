package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.service.BaseService;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.ListCrudRepository;

public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {
    @Override
    public @NonNull T findById(ID id) {
        return getRepository().findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public @NonNull List<T> findAll() {
        return getRepository().findAll();
    }

    protected abstract @Nonnull ListCrudRepository<T, ID> getRepository();
}
