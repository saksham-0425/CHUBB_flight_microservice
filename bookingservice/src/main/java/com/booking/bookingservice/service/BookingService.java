package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.EmailNotification;
import com.booking.bookingservice.dto.FlightInternalDTO;
import com.booking.bookingservice.feign.FlightClient;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.producer.EmailProducer;
import com.booking.bookingservice.repo.BookingRepository;
import com.booking.bookingservice.exception.BookingNotFoundException;
import com.booking.bookingservice.exception.CancellationNotAllowedException;
import com.booking.bookingservice.exception.FlightServiceDownException;
import com.booking.bookingservice.util.PnrGenerator;   // ⭐ IMPORT
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository repository;
    private final FlightClient flightClient;
    private final EmailProducer emailProducer;

    public BookingService(BookingRepository repository,
                          FlightClient flightClient,
                          EmailProducer emailProducer) {
        this.repository = repository;
        this.flightClient = flightClient;
        this.emailProducer = emailProducer;
    }

    @CircuitBreaker(name = "flightservice", fallbackMethod = "bookFallback")
    public Booking bookTicket(BookingRequest req) {

        log.info("Attempt to book {} seats on flight {}", req.getSeats(), req.getFlightId());

        boolean available = flightClient.checkAvailability(req.getFlightId());

        Optional<Booking> existing =
                repository.findByFlightIdAndEmail(req.getFlightId(), req.getEmail());

        if (existing.isPresent()) {
            throw new IllegalStateException("Booking already exists for this user and flight");
        }

        if (!available) {
            throw new FlightServiceDownException("Flight Service is DOWN");
        }

        boolean reduced = flightClient.reduceSeats(req.getFlightId(), req.getSeats());
        if (!reduced) {
            throw new FlightServiceDownException("Flight Service is DOWN");
        }

        Booking booking = new Booking();
        booking.setPnr(PnrGenerator.generate());      
        booking.setFlightId(req.getFlightId());
        booking.setPassengerName(req.getPassengerName());
        booking.setEmail(req.getEmail());
        booking.setSeats(req.getSeats());
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(new Date());

        repository.save(booking);

        emailProducer.sendEmail(new EmailNotification(
                booking.getEmail(),
                "Booking Confirmed",
                "Your booking is confirmed.\n" +
                "PNR: " + booking.getPnr() + "\n" +     // ⭐ PNR IN EMAIL
                "Flight ID: " + booking.getFlightId()
        ));

        log.info("Booking successful: PNR={}", booking.getPnr());
        return booking;
    }

    // ---------------- FALLBACK ----------------

    public Booking bookFallback(BookingRequest req, Throwable ex) {

        log.warn("Fallback triggered due to: {}", ex.toString());

        Booking fallbackBooking = new Booking();
        fallbackBooking.setPnr(PnrGenerator.generate());   // ⭐ EVEN FAILED HAS PNR
        fallbackBooking.setPassengerName(req.getPassengerName());
        fallbackBooking.setEmail(req.getEmail());
        fallbackBooking.setFlightId(req.getFlightId());
        fallbackBooking.setSeats(req.getSeats());
        fallbackBooking.setStatus("FAILED");
        fallbackBooking.setBookingDate(new Date());

        return fallbackBooking;
    }

    // ---------------- CANCEL BOOKING ----------------

    public Booking cancelBooking(String id) {

        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));

        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        booking.setStatus("CANCELLED");      // ⭐ STATUS CHANGES, PNR UNTOUCHED
        repository.save(booking);

        flightClient.increaseSeats(booking.getFlightId(), booking.getSeats());

        emailProducer.sendEmail(new EmailNotification(
                booking.getEmail(),
                "Booking Cancelled",
                "Your booking has been cancelled.\n" +
                "PNR: " + booking.getPnr()              // ⭐ PNR STILL PRESENT
        ));

        return booking;
    }

    // ---------------- GET APIs ----------------

    public Booking getBooking(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    public List<Booking> getBookingsByEmail(String email) {
        return repository.findByEmail(email);
    }
    
    public Booking getBookingByPnr(String pnr) {
        return repository.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException("PNR: " + pnr));
    }

    public Booking cancelBookingByPnr(String pnr) {

        Booking booking = repository.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException("PNR: " + pnr));

        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        // ⭐ Fetch flight details
        FlightInternalDTO flight = flightClient.getFlightById(booking.getFlightId());

        if (flight == null || flight.getDate() == null) {
            throw new FlightServiceDownException("Unable to verify flight schedule");
        }

        // ⭐ Parse flight date (String → LocalDate)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate flightDate;

        try {
            flightDate = LocalDate.parse(flight.getDate(), formatter);
        } catch (Exception e) {
            throw new FlightServiceDownException("Invalid flight date format");
        }

        // ⭐ Calculate hours until flight
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime flightDateTime = flightDate.atStartOfDay();

        long hoursLeft = ChronoUnit.HOURS.between(now, flightDateTime);

        if (hoursLeft < 24) {
            throw new CancellationNotAllowedException(
                    "Ticket cannot be cancelled within 24 hours of flight departure"
            );
        }

        // ✅ Cancel booking
        booking.setStatus("CANCELLED");
        repository.save(booking);

        // Restore seats
        flightClient.increaseSeats(booking.getFlightId(), booking.getSeats());

        // Notify user
        emailProducer.sendEmail(new EmailNotification(
                booking.getEmail(),
                "Booking Cancelled",
                "Your booking has been cancelled.\nPNR: " + booking.getPnr()
        ));

        log.info("Booking cancelled by PNR: {}", pnr);

        return booking;
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }
    
}
