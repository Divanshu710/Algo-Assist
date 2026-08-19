package com.codingapp.testservice.exception;

public class InvalidTestStateException extends RuntimeException {
    public InvalidTestStateException(String message) {
        super(message);
    }
}
