package com.booking.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class BookingResponse {

    private String pnr;
    private String flightId;
    private String passengerName;
    private String email;
    private int seats;
    private String status;
    private Date bookingDate;
}
