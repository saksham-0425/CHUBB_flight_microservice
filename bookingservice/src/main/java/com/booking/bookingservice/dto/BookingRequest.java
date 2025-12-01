package com.booking.bookingservice.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private String flightId;
    private String passengerName;
    private String email;
    private int seats;
}
