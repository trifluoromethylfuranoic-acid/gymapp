package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.dto.request.CreateUserRequest;
import com.epam.lenda.gymapp.dto.request.UpdateUserRequest;
import com.epam.lenda.gymapp.exception.IllegalStateTransitionException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.IsUser;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.BaseUserRepository;
import com.epam.lenda.gymapp.service.BaseUserService;
import com.epam.lenda.gymapp.service.CredentialsService;
import com.epam.lenda.gymapp.util.Pair;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseUserServiceImpl<T extends IsUser, C extends CreateUserRequest, U extends UpdateUserRequest> extends BaseServiceImpl<T, UUID> implements BaseUserService<T, C, U> {
    private final Counter signupsCounter;

    public BaseUserServiceImpl(MeterRegistry meterRegistry) {
        this.signupsCounter = meterRegistry.counter("signups.successful");
    }

    @Transactional
    public @Nonnull T findByUsername(@Nonnull String username) {
        return getRepository().findByUsername(username).orElseThrow(() -> new ResourceNotFoundException(
                getResourceName(), username));
    }

    @Override
    @Transactional
    public @Nonnull T updateActiveStatus(@Nonnull String username, boolean isActive) {
        final var entity = findByUsername(username);
        final var user = entity.getUser();

        if (isActive == user.getIsActive()) {
            final var activeString = isActive ? "active" : "inactive";
            throw new IllegalStateTransitionException(activeString, activeString);
        }

        user.setIsActive(isActive);

        return entity;
    }

    protected @Nonnull Pair<User, String> createUser(@Nonnull C request) {
        final var credentials = getCredentialsService().generateCredentials(request);
        final var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName()).username(
                credentials.username()).password(credentials.passwordHash()).isActive(true).build();
        signupsCounter.increment();
        return Pair.of(user, credentials.password());
    }

    protected void updateUser(@Nonnull User user, @Nonnull U request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());
    }

    @Override
    protected abstract @Nonnull BaseUserRepository<T> getRepository();

    protected abstract @Nonnull CredentialsService getCredentialsService();
}
