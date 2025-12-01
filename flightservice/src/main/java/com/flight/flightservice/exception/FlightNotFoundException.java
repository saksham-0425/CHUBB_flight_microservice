package com.flight.flightservice.exception;

@SuppressWarnings("serial")
public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(String id) {
        super("Flight not found with ID: " + id);
    }
}
