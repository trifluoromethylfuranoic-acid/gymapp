package com.epam.lenda.gymapp.dto.request;

import com.epam.lenda.gymapp.model.Training;
import com.epam.lenda.gymapp.util.SpecificationUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Builder
@AllArgsConstructor
@Jacksonized
public class SearchTrainingRequest {
    @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime fromDateInclusive;

    @Nullable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime toDateInclusive;

    @Nullable private String traineeUsername;

    @Nullable private String trainerUsername;

    public @Nonnull Specification<Training> toSpecification() {
        return SpecificationUtil.<Training, ZonedDateTime>fieldBetweenInclusive("datetime", fromDateInclusive,
                toDateInclusive).and(
                        traineeUsernameEquals()).and(trainerUsernameEquals());
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
