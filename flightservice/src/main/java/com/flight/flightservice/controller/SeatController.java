package com.flight.flightservice.controller;

import com.flight.flightservice.dto.SeatResponse;
import com.flight.flightservice.repo.FlightRepository;
import com.flight.flightservice.repo.SeatRepository;
import com.flight.flightservice.service.SeatSeederService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.flight.flightservice.model.*;
@RestController
@RequestMapping("/flights")
public class SeatController {

    private final SeatRepository seatRepository;
    private final SeatSeederService seatSeederService;
    private final FlightRepository flightRepository;

    public SeatController(
            SeatRepository seatRepository,
            SeatSeederService seatSeederService,
            FlightRepository flightRepository
    ) {
        this.seatRepository = seatRepository;
        this.seatSeederService = seatSeederService;
        this.flightRepository = flightRepository;
    }


    @GetMapping("/{flightId}/seats")
    public List<SeatResponse> getSeatMap(@PathVariable String flightId) {

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        seatSeederService.seedSeatsIfRequired(
                flight.getId(),
                flight.getAvailableSeats()
        );

        return seatRepository.findByFlightId(flightId)
                .stream()
                .map(seat -> new SeatResponse(
                        seat.getSeatNumber(),
                        seat.isBooked()
                ))
                .toList();
    }


    @PostMapping("/{flightId}/seats/lock")
    public void lockSeats(
            @PathVariable String flightId,
            @RequestBody List<String> seatNumbers
    ) {
        List<Seat> seats = seatRepository
                .findByFlightIdAndSeatNumberIn(flightId, seatNumbers);

        if (seats.size() != seatNumbers.size()) {
            throw new RuntimeException("One or more seats do not exist");
        }

        if (seats.stream().anyMatch(Seat::isBooked)) {
            throw new RuntimeException("Seat already booked");
        }

        seats.forEach(seat -> seat.setBooked(true));
        seatRepository.saveAll(seats);
    }

    @PostMapping("/{flightId}/seats/release")
    public void releaseSeats(
            @PathVariable String flightId,
            @RequestBody List<String> seatNumbers
    ) {
        List<Seat> seats = seatRepository
                .findByFlightIdAndSeatNumberIn(flightId, seatNumbers);

        seats.forEach(seat -> seat.setBooked(false));
        seatRepository.saveAll(seats);
    }
}
