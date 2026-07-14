package com.epam.lenda.gymapp.exception;

public class WrongCredentialsException extends RuntimeException {
    public WrongCredentialsException() {
    }

    public WrongCredentialsException(String message) {
        super(message);
    }
}
