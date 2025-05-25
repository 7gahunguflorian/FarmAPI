package com.farm.delivery.farmapi.exception;

public class NonAdminAccessException extends RuntimeException {
    public NonAdminAccessException(String message) {
        super(message);
    }
} 