package com.booking.bookingservice.exception;

@SuppressWarnings("serial")
public class FlightServiceDownException extends RuntimeException {
    public FlightServiceDownException(String message) {
        super(message);
    }
}
