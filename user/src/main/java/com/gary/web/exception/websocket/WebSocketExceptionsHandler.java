package com.gary.web.exception.websocket;

import com.gary.web.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.naming.ServiceUnavailableException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class WebSocketExceptionsHandler {

    @MessageExceptionHandler(ServiceUnavailableException.class)
    public ErrorResponse handleServiceUnavailableException(ServiceUnavailableException ex) {
        return new ErrorResponse(
                ex.getMessage(),
                HttpStatus.SERVICE_UNAVAILABLE,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
    }

    @MessageExceptionHandler(TooManyRequestsException.class)
    public ErrorResponse handleTooManyRequests(TooManyRequestsException e) {
        return new ErrorResponse(
                e.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
    }

    @MessageExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(Exception e) {
        // log.error("Unhandled WS exception", e);
        return new ErrorResponse(
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
    }
}
