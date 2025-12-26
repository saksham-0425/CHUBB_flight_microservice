package com.booking.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {
    private String flightId;
    private String passengerName;
    private String email;

    private int passengerCount;         
    private List<String> seatNumbers;     
}
