package com.booking.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {

    private String pnr;
    private String flightId;
    private String passengerName;
    private String email;

    // ✅ NEW
    private int passengerCount;
    private List<String> seatNumbers;

    private String status;
    private Date bookingDate;
}
