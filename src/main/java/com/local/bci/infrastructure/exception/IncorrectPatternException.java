package com.local.bci.infrastructure.exception;

public class IncorrectPatternException extends IllegalArgumentException {
    public IncorrectPatternException(String message) {
        super(message);
    }
}
