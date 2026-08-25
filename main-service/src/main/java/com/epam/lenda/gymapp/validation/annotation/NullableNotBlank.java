package com.epam.lenda.gymapp.validation.annotation;

import com.epam.lenda.gymapp.validation.NullableNotBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NullableNotBlankValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NullableNotBlank {
    String message() default "must not be blank if present";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
