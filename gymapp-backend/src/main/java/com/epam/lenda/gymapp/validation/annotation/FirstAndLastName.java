package com.epam.lenda.gymapp.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {})
@Pattern(regexp = "^[a-zA-Z\\-,' .]+$")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FirstAndLastName {
    String message() default "name can only contain letters, spaces and - ' .";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
