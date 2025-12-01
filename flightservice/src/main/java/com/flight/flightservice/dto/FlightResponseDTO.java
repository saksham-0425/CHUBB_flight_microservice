package com.flight.flightservice.dto;

public class FlightResponseDTO {

    private String id;
    private String airline;
    private String source;
    private String destination;

    private int availableSeats;


    public FlightResponseDTO() {

    }

    public FlightResponseDTO(String id,
                             String airline,
                             String source,
                             String destination,

                             int availableSeats
                             ) {
        this.id = id;
        this.airline = airline;
        this.source = source;
        this.destination = destination;

        this.availableSeats = availableSeats;

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }


    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

}

