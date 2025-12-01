package com.booking.bookingservice.feign;

import org.springframework.stereotype.Component;

@Component
public class FlightClientFallback implements FlightClient {

    @Override
    public boolean checkAvailability(String id) {
        return false;
    }

    @Override
    public boolean reduceSeats(String id, int count) {
        return false;  
    }

    @Override
    public void increaseSeats(String id, int count) {
 
    }
}

