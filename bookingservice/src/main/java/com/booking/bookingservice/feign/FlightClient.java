package com.booking.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.booking.bookingservice.dto.FlightInternalDTO;
import java.util.*;

@FeignClient(name = "flightservice", fallback = FlightClientFallback.class)
public interface FlightClient {

    @GetMapping("/flights/internal/{id}/check")
    boolean checkAvailability(@PathVariable("id") String id);

    @PutMapping("/flights/internal/{id}/reduce")
    boolean reduceSeats(@PathVariable("id") String id, @RequestParam("count") int count);

    @PutMapping("/flights/internal/{id}/increase")
    void increaseSeats(@PathVariable("id") String id, @RequestParam("count") int count);
   
    @GetMapping("/flights/internal/{id}")
    FlightInternalDTO getFlightById(@PathVariable("id") String id);
    
    @PostMapping("/flights/{flightId}/seats/lock")
    void lockSeats(
        @PathVariable String flightId,
        @RequestBody List<String> seatNumbers
    );

    @PostMapping("/flights/{flightId}/seats/release")
    void releaseSeats(
        @PathVariable String flightId,
        @RequestBody List<String> seatNumbers
    );
}
