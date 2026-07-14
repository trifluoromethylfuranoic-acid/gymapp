package com.epam.lenda.gymapp.service.impl;

import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.model.IsUser;
import com.epam.lenda.gymapp.model.User;
import com.epam.lenda.gymapp.repository.BaseUserRepository;
import com.epam.lenda.gymapp.service.AuthService;
import com.epam.lenda.gymapp.service.BaseUserService;
import com.epam.lenda.gymapp.service.CredentialsService;
import com.epam.lenda.gymapp.util.Pair;
import jakarta.annotation.Nonnull;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseUserServiceImpl<T extends IsUser, C extends BaseUserService.UserCreateRequest, U extends BaseUserService.UserUpdateRequest> extends BaseServiceImpl<T, UUID> implements BaseUserService<T, C, U> {

    @Transactional
    public @Nonnull T findByUsername(@Nonnull String username) {
        return getRepository().findByUsername(username).orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    @Transactional
    public @NonNull T updatePassword(@NonNull String username, @Nonnull PasswordChangeRequest request) {
        final var authentication = new AuthService.AuthenticationRequest(username, request.getOldPassword());
        getAuthService().requireAuthentication(authentication);

        final var entity = findByUsername(username);
        final var user = entity.getUser();
        final var passwordHash = getCredentialsService().encodePassword(request.getNewPassword());

        user.setPassword(passwordHash);

        return entity;
    }

    @Override
    @Transactional
    public @Nonnull T toggleActivation(@Nonnull String username) {
        final var entity = findByUsername(username);
        final var user = entity.getUser();

        user.setIsActive(!user.getIsActive());

        return entity;
    }

    protected @Nonnull Pair<User, String> createUser(@Nonnull C request) {
        final var credentials = getCredentialsService().generateCredentials(request);
        final var user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName()).username(
                credentials.username()).password(credentials.passwordHash()).isActive(true).build();
        return Pair.of(user, credentials.password());
    }

    protected void updateUser(@Nonnull User user, @Nonnull U request) {
        if (getCredentialsService().isUsernameTaken(request.getUsername(), user.getId())) {
            throw new DuplicateUsernameException();
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
    }

    @Override
    protected abstract @Nonnull BaseUserRepository<T> getRepository();

    protected abstract @Nonnull CredentialsService getCredentialsService();

    protected abstract @Nonnull AuthService getAuthService();
}
