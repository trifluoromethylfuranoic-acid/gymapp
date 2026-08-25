package com.epam.lenda.gymapp.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record TraineeResponse(
                              @Nonnull String username,
                              @Nonnull String firstName,
                              @Nonnull String lastName,
                              String address,
                              @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING) LocalDate dateOfBirth,
                              boolean isActive
) {
}
