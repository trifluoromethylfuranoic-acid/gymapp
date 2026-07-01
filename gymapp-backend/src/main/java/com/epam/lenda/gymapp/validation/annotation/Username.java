package com.epam.lenda.gymapp.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@Pattern(regexp = "^[a-zA-Z0-9\\-,' ._]+$")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {
    String message() default "username can only contain letters, digits, spaces and - ' . _";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
