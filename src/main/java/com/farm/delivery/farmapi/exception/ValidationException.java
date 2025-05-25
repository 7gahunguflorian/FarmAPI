package com.farm.delivery.farmapi.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String fieldName, String message) {
        super(String.format("Validation failed for %s: %s", fieldName, message));
    }
} 