package com.epam.lenda.gymapp.exception;

import lombok.Getter;

@Getter
public class IllegalStateTransitionException extends RuntimeException {
    private final String oldState;
    private final String newState;


    public IllegalStateTransitionException() {
        super("Illegal action");
        this.oldState = null;
        this.newState = null;
    }

    public IllegalStateTransitionException(String message) {
        super(message);
        this.oldState = null;
        this.newState = null;
    }

    public IllegalStateTransitionException(String oldState, String newState) {
        super("Cannot transition from %s state to %s state".formatted(oldState, newState));
        this.oldState = oldState;
        this.newState = newState;
    }
}
