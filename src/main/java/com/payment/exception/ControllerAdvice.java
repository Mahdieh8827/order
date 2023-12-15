package com.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedMerchantException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleDeniedAccessException(AccessDeniedMerchantException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(CreateTokenException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(CreateTokenException ex) {
        return new ErrorResponse("An error occurred while creating a token.");
    }

    @ExceptionHandler(InvalidPaymentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleException(InvalidPaymentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        return new ErrorResponse("An error occurred while processing the request.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleException(AccessDeniedException ex) {
        return new ErrorResponse("Access is denied");
    }
}
