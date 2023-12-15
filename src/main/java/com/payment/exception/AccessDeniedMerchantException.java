package com.payment.exception;

public class AccessDeniedMerchantException extends RuntimeException {
    public AccessDeniedMerchantException(String message)
    {
       super(message);
    }
}
