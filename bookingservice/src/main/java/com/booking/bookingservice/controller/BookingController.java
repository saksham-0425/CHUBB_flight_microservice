package com.booking.bookingservice.controller;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.BookingResponse;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a booking")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest req) {

        log.info("Create booking request for flight {}", req.getFlightId());

        Booking booking = service.bookTicket(req);

        BookingResponse response = new BookingResponse(
                booking.getPnr(),
                booking.getFlightId(),
                booking.getPassengerName(),
                booking.getEmail(),
                booking.getSeats(),
                booking.getStatus(),
                booking.getBookingDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PutMapping("/cancel/{id}")
    @Operation(summary = "Cancel booking")
    public Booking cancelBooking(@PathVariable String id) {
        log.info("Cancel booking {}", id);
        return service.cancelBooking(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by id")
    public Booking getBooking(@PathVariable String id) {
        return service.getBooking(id);
    }

    @GetMapping("/history")
    @Operation(summary = "Get bookings by email")
    public List<Booking> history(@RequestParam String email) {
        return service.getBookingsByEmail(email);
    }
    
    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getBookingByPnr(
            @PathVariable String pnr) {

        Booking booking = service.getBookingByPnr(pnr);

        BookingResponse response = new BookingResponse(
                booking.getPnr(),
                booking.getFlightId(),
                booking.getPassengerName(),
                booking.getEmail(),
                booking.getSeats(),
                booking.getStatus(),
                booking.getBookingDate()
        );

        return ResponseEntity.ok(response);
    }

    
    @PutMapping("/cancel/pnr/{pnr}")
    @Operation(summary = "Cancel booking by PNR")
    public BookingResponse cancelBookingByPnr(@PathVariable String pnr) {

        log.info("Cancel booking by PNR {}", pnr);

        Booking booking = service.cancelBookingByPnr(pnr);

        return new BookingResponse(
                booking.getPnr(),
                booking.getFlightId(),
                booking.getPassengerName(),
                booking.getEmail(),
                booking.getSeats(),
                booking.getStatus(),
                booking.getBookingDate()
        );
    }


}
