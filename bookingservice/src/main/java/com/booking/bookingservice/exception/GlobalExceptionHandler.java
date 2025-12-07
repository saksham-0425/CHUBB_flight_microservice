package com.booking.bookingservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 NOT FOUND
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // 400 BAD REQUEST
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidArgs(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // 409 CONFLICT (duplicate booking or business rule violations)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // 503 SERVICE UNAVAILABLE
    @ExceptionHandler(FlightServiceDownException.class)
    public ResponseEntity<String> handleFlightServiceDown(FlightServiceDownException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    // 503 when flight service is down but thrown as RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Flight Service is DOWN")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        }

        log.error("Unexpected error:", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }
}
