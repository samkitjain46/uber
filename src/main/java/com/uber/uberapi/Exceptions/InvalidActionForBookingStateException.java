package com.uber.uberapi.Exceptions;

public class InvalidActionForBookingStateException extends UberException {

    public InvalidActionForBookingStateException(String message) {
        super(message);
    }
}
