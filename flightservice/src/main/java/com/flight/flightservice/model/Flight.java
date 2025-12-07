package com.flight.flightservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Document(collection = "flights")
public class Flight {

    @Id
    private String id;

    @NotBlank(message = "Flight number is required")
    @Size(min = 3, max = 10, message = "Flight number must be between 3 and 10 characters")
    private String flightNumber;

    @NotBlank(message = "Airline is required")
    private String airline;

    @NotBlank(message = "Source airport is required")
    private String source;

    @NotBlank(message = "Destination airport is required")
    private String destination;

    @NotBlank(message = "Date is required")
    private String date; 

    @NotNull(message = "Available seats is required")
    @Min(value = 1, message = "Available seats must be at least 1")
    private Integer availableSeats;
}
