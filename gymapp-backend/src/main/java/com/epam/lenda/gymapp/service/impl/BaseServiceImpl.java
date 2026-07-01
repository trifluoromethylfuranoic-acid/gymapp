package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.repository.BaseRepository;
import com.epam.lenda.gymapp.service.BaseService;
import java.util.List;
import org.jspecify.annotations.NonNull;

public abstract class BaseServiceImpl<T> implements BaseService<T> {
    protected final BaseRepository<T> repo;

    public BaseServiceImpl(BaseRepository<T> repo) {
        this.repo = repo;
    }

    @Override
    public @NonNull T findById(long id) {
        return repo.findById(id).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public @NonNull List<T> findAll() {
        return repo.findAll();
    }
}
