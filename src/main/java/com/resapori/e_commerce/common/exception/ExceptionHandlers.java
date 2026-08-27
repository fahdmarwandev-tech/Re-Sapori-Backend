package com.resapori.e_commerce.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exc) {
        return buildResponseEntity(exc, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exc) {
        return buildResponseEntity(exc, HttpStatus.valueOf(exc.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exc) {
        return buildResponseEntity(exc, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<ErrorResponse> buildResponseEntity(Exception exc, HttpStatus status) {
        ErrorResponse error = new ErrorResponse();
        error.setMessage(exc.getMessage());
        error.setStatus(status.value());
        error.setTimestamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, status);
    }
}
