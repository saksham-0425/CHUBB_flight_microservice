package com.booking.bookingservice.feign;

import com.booking.bookingservice.dto.FlightInternalDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightClientFallback implements FlightClient {

  

    @Override
    public boolean checkAvailability(String id) {

        return false;
    }

    @Override
    public boolean reduceSeats(String flightId, int count) {
        
        return false;
    }

    @Override
    public void increaseSeats(String flightId, int count) {
        
    }



    @Override
    public void lockSeats(String flightId, List<String> seatNumbers) {
        
        throw new IllegalStateException(
                "Flight Service unavailable (seat lock failed)"
        );
    }

    @Override
    public void releaseSeats(String flightId, List<String> seatNumbers) {

    }

   

    @Override
    public FlightInternalDTO getFlightById(String flightId) {
        
        return null;
    }
}
