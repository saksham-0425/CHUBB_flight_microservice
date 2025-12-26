package com.flight.flightservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatResponse {
    private String seatNumber;
    private boolean booked;
}
