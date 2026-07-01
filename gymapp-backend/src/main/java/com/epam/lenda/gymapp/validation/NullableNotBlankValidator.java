package com.epam.lenda.gymapp.validation;

import com.epam.lenda.gymapp.validation.annotation.NullableNotBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NullableNotBlankValidator implements ConstraintValidator<NullableNotBlank, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || !value.trim().isEmpty();
    }
}
