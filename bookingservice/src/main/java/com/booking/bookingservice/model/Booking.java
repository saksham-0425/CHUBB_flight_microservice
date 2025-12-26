package com.booking.bookingservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private String pnr;
    private String flightId;
    private String passengerName;
    private String email;

   
    private int passengerCount;

    
    private List<String> seatNumbers;

    private String status;
    private Date bookingDate;
}
