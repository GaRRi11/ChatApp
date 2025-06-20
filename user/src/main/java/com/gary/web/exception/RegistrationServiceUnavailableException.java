package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RegistrationServiceUnavailableException extends RuntimeException {
    public RegistrationServiceUnavailableException(String message) {
        super(message);
    }
    public RegistrationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
