package com.local.bci.infrastructure.exception;

public class UserPersistenceException extends RuntimeException {
    public UserPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
