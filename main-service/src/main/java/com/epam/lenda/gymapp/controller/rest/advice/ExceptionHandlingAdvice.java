package com.epam.lenda.gymapp.controller.rest.advice;

import com.epam.lenda.gymapp.dto.response.ValidationErrorResponse;
import com.epam.lenda.gymapp.exception.DuplicateUsernameException;
import com.epam.lenda.gymapp.exception.IllegalStateTransitionException;
import com.epam.lenda.gymapp.exception.ResourceNotFoundException;
import com.epam.lenda.gymapp.exception.WrongCredentialsException;
import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackages = "com.epam.lenda.gymapp.controller.rest")
public class ExceptionHandlingAdvice extends ResponseEntityExceptionHandler {
    private final int blockDurationMinutes;

    public ExceptionHandlingAdvice(
            @Value("${application.security.userBlock.durationSeconds}") int blockDurationSeconds
    ) {
        blockDurationMinutes = blockDurationSeconds / 60;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @Nonnull MethodArgumentNotValidException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request) {

        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                             new ValidationErrorResponse(error.getField(), String.valueOf(error.getRejectedValue()),
                                                         error.getDefaultMessage()))
                .toList());

        return ResponseEntity.status(status).headers(headers).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(@Nonnull ConstraintViolationException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", ex
                .getConstraintViolations()
                .stream()
                .map(error ->
                             new ValidationErrorResponse(error.getPropertyPath().toString(),
                                                         String.valueOf(error.getInvalidValue()),
                                                         error.getMessage())).toList());
        return problem;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(@Nonnull ResourceNotFoundException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Not found");
        if (ex.getResourceIds() != null) {
            problem.setProperty("resourceIds", ex.getResourceIds());
        }
        if (ex.getResourceType() != null) {
            problem.setProperty("resourceType", ex.getResourceType());
        }
        return problem;
    }

    @ExceptionHandler(WrongCredentialsException.class)
    public ProblemDetail handleWrongCredentials(@Nonnull WrongCredentialsException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        problem.setTitle("Authentication failed");
        return problem;
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ProblemDetail handleDuplicateUsername(@Nonnull DuplicateUsernameException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Username already in use");
        problem.setTitle("Username already in use");
        return problem;
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ProblemDetail handleIllegalStateTransition(@Nonnull IllegalStateTransitionException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Illegal action");
        return problem;
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleIllegalStateTransition(@Nonnull LockedException ex) {
        final var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.LOCKED,
                "Account locked due to too many login attempts. Try again in %s minutes".formatted(
                        blockDurationMinutes));
        problem.setTitle("Account locked");
        return problem;
    }
}
