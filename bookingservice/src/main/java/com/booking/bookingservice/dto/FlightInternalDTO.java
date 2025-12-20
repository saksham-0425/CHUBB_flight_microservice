package com.booking.bookingservice.dto;

import lombok.Data;

@Data
public class FlightInternalDTO {

    private String id;
    private String airline;
    private String source;
    private String destination;
    private String date;       
    private Integer availableSeats;
}
