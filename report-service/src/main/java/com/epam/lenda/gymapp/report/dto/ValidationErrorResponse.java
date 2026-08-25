package com.epam.lenda.gymapp.report.dto;

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
