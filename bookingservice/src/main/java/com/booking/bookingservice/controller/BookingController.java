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

    private static final Logger log =
            LoggerFactory.getLogger(BookingController.class);

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }


    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Email") String userEmail,
            @RequestBody BookingRequest req) {

        log.info("Create booking request by {}", userEmail);

        req.setEmail(userEmail); // 🔒 enforce ownership

        Booking booking = service.bookTicket(req);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(booking));
    }


    @PutMapping("/cancel/{id}")
    @Operation(summary = "Cancel booking by ID")
    public Booking cancelBooking(@PathVariable String id) {
        log.info("Cancel booking {}", id);
        return service.cancelBooking(id);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public Booking getBooking(@PathVariable String id) {
        return service.getBooking(id);
    }


    @GetMapping("/history")
    public List<Booking> history(
            @RequestParam(required = false) String email,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Roles") String roles) {

        if (isAdmin(roles)) {
            return email != null
                    ? service.getBookingsByEmail(email)
                    : service.getAllBookings();
        }

        return service.getBookingsByEmail(userEmail);
    }


    @GetMapping("/pnr/{pnr}")
    public ResponseEntity<BookingResponse> getBookingByPnr(
            @PathVariable String pnr,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Roles") String roles) {

        Booking booking = service.getBookingByPnr(pnr);

        if (!isAdmin(roles) && !booking.getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(toResponse(booking));
    }


    @PutMapping("/cancel/pnr/{pnr}")
    public ResponseEntity<BookingResponse> cancelBookingByPnr(
            @PathVariable String pnr,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Roles") String roles) {

        Booking booking = service.getBookingByPnr(pnr);

        if (!isAdmin(roles) && !booking.getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Booking cancelled = service.cancelBookingByPnr(pnr);

        return ResponseEntity.ok(toResponse(cancelled));
    }


    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getPnr(),
                booking.getFlightId(),
                booking.getPassengerName(),
                booking.getEmail(),
                booking.getPassengerCount(),
                booking.getSeatNumbers(),
                booking.getStatus(),
                booking.getBookingDate()
        );
    }

    private boolean isAdmin(String rolesHeader) {
        return rolesHeader != null && rolesHeader.contains("ROLE_ADMIN");
    }
}
