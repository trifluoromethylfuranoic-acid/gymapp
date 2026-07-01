package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Trainee;
import com.epam.lenda.gymapp.util.Pair;
import com.epam.lenda.gymapp.validation.annotation.FirstAndLastName;
import com.epam.lenda.gymapp.validation.annotation.Password;
import com.epam.lenda.gymapp.validation.annotation.Username;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import lombok.Builder;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TraineeService extends UpdatableUserService<Trainee, TraineeService.UpdateRequest>, DeletableUserService<Trainee> {
    /**
     * Creates and saves new trainee with generated credentials
     *
     * @return pair of created trainee and their password
     */
    @Nonnull
    Pair<Trainee, String> create(@NotBlank @Valid @FirstAndLastName String firstName,
                                 @NotBlank @Valid @FirstAndLastName String lastName,
                                 @NotNull @Valid @Past LocalDate dateOfBirth,
                                 @NotBlank @Valid String address);

    @Builder
    record UpdateRequest(
                         @NotBlank @Username String username,
                         @NotBlank @Password String password,
                         boolean isActive,
                         @NotBlank @FirstAndLastName String firstName,
                         @NotBlank @FirstAndLastName String lastName,
                         @NotNull @Past LocalDate dateOfBirth,
                         @NotBlank String address
    ) {
    }
}
