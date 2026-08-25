package com.epam.lenda.gymapp.dto.response;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ValidationErrorResponse(
                                      String field,
                                      String rejectedValue,
                                      String message
) {
}
