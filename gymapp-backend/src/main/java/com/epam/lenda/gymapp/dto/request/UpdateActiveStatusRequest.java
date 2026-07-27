package com.epam.lenda.gymapp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record UpdateActiveStatusRequest(
                                        @NotNull Boolean isActive
) {
}
