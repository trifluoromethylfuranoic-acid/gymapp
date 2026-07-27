package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.service.BaseService;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

public abstract class BaseServiceImpl<T, ID> implements BaseService<T, ID> {
    @Override
    public @Nonnull T findById(ID id) {
        return getRepository().findById(id).orElseThrow(() -> new ResourceNotFoundException(getResourceName(), id));
    }

    @Override
    public @Nonnull List<T> findAll() {
        return getRepository().findAll();
    }

    protected abstract @Nonnull ListCrudRepository<T, ID> getRepository();

    protected abstract @Nonnull String getResourceName();
}
