package com.epam.lenda.gymapp.dto.trainee;

import java.time.LocalDate;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SearchTraineeRequest(
                                   String nameQuery,
                                   LocalDate dateOfBirthMin,
                                   LocalDate dateOfBirthMax,
                                   String addressQuery,
                                   String assignedTo,
                                   String notAssignedTo
) {
}