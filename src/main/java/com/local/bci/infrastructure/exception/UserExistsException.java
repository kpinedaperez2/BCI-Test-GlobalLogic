package com.local.bci.infrastructure.exception;

public class UserExistsException extends IllegalStateException {
    public UserExistsException(String message) {
        super(message);
    }
}
