package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.BaseUserRepository;
import com.epam.lenda.gymapp.service.BaseUserService;
import org.jspecify.annotations.NonNull;

public abstract class BaseUserServiceImpl<T extends User> extends BaseServiceImpl<T> implements BaseUserService<T> {
    protected final BaseUserRepository<T> repo;

    public BaseUserServiceImpl(BaseUserRepository<T> repo) {
        super(repo);
        this.repo = repo;
    }

    @Override
    public @NonNull T findByUsername(@NonNull String username) {
        return repo.findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    }
}
