package com.flight.flightservice.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "seats")
@Data
public class Seat {

    @Id
    private String id;

    private String flightId;
    private String seatNumber;  
    private boolean booked;
}

