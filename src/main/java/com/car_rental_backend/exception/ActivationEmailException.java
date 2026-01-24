package com.car_rental_backend.exception;

/**
 * Thrown when activation email cannot be sent (e.g. SMTP failure).
 */
public class ActivationEmailException extends RuntimeException {

    public ActivationEmailException(String message) {
        super(message);
    }

    public ActivationEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
