package com.epam.lenda.gymapp.service;

import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.util.SpecificationUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TrainingService extends BaseService<Training, UUID> {
    @Nonnull
    Training create(@NotNull @Valid TrainingCreateRequest request);

    @Nonnull
    List<Training> search(@Nonnull TrainingSearchRequest request);

    @Getter
    @Builder
    @AllArgsConstructor
    @Jacksonized
    class TrainingSearchRequest {
        @Nullable private Date fromDateInclusive;

        @Nullable private Date toDateInclusive;

        @Nullable private String traineeUsername;

        @Nullable private String trainerUsername;

        public @Nonnull Specification<Training> toSpecification() {
            return SpecificationUtil.<Training, Date>fieldBetweenInclusive("datetime", fromDateInclusive,
                    toDateInclusive).and(traineeUsernameEquals()).and(trainerUsernameEquals());
        }

        private @Nonnull Specification<Training> traineeUsernameEquals() {
            return (root, query, criteriaBuilder) -> {
                if (traineeUsername == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("trainee").get("user").get("username"), traineeUsername);
            };
        }

        private @Nonnull Specification<Training> trainerUsernameEquals() {
            return (root, query, criteriaBuilder) -> {
                if (trainerUsername == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("trainer").get("user").get("username"), trainerUsername);
            };
        }
    }

    @Getter
    @AllArgsConstructor
    @Builder
    @Jacksonized
    class TrainingCreateRequest {
        @NotBlank
        String trainee;

        @NotBlank
        String trainer;

        @NotBlank
        String name;

        @NotBlank
        String type;

        @NotNull Date datetime;

        @Positive int durationMinutes;
    }
}
