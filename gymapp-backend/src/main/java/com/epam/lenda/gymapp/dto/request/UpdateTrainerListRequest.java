package com.epam.lenda.gymapp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record UpdateTrainerListRequest(
                                       @NotNull List<@NotNull String> trainers
) {
}
