package com.uber.uberapi.Exceptions;

public class InvalidOTPException extends UberException {
    public InvalidOTPException() {
        super("Invalid OTP");
    }
}
