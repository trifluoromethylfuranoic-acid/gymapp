package com.epam.lenda.gymapp.exception;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
    }

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
