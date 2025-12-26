package com.flight.flightservice.service;

import com.flight.flightservice.model.Seat;
import com.flight.flightservice.repo.SeatRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatSeederService {

    private final SeatRepository seatRepository;

    private static final int SEATS_PER_ROW = 6; // A–F

    public SeatSeederService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public void seedSeatsIfRequired(String flightId, int totalSeats) {

        if (!seatRepository.findByFlightId(flightId).isEmpty()) {
            return;
        }

        List<Seat> seats = new ArrayList<>();

        int rows = (int) Math.ceil((double) totalSeats / SEATS_PER_ROW);

        int seatCount = 0;

        for (int row = 0; row < rows && seatCount < totalSeats; row++) {
            char rowLetter = (char) ('A' + row);

            for (int col = 1; col <= SEATS_PER_ROW && seatCount < totalSeats; col++) {
                Seat seat = new Seat();
                seat.setFlightId(flightId);
                seat.setSeatNumber(rowLetter + String.valueOf(col));
                seat.setBooked(false);

                seats.add(seat);
                seatCount++;
            }
        }

        seatRepository.saveAll(seats);
    }
}
