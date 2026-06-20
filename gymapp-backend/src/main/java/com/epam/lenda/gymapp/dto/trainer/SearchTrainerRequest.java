package com.epam.lenda.gymapp.dto.trainer;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SearchTrainerRequest(
                                   String nameQuery,
                                   String specializationQuery,
                                   String assignedTo,
                                   String notAssignedTo
) {
}