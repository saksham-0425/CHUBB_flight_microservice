package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.EmailNotification;
import com.booking.bookingservice.dto.FlightInternalDTO;
import com.booking.bookingservice.exception.BookingNotFoundException;
import com.booking.bookingservice.exception.CancellationNotAllowedException;
import com.booking.bookingservice.exception.FlightServiceDownException;
import com.booking.bookingservice.feign.FlightClient;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.producer.EmailProducer;
import com.booking.bookingservice.repo.BookingRepository;
import com.booking.bookingservice.util.PnrGenerator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class BookingService {

    private static final Logger log =
            LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final EmailProducer emailProducer;

    public BookingService(
            BookingRepository bookingRepository,
            FlightClient flightClient,
            EmailProducer emailProducer
    ) {
        this.bookingRepository = bookingRepository;
        this.flightClient = flightClient;
        this.emailProducer = emailProducer;
    }


    @CircuitBreaker(name = "flightservice", fallbackMethod = "bookFallback")
    @Transactional
    public Booking bookTicket(BookingRequest req) {

        log.info("Booking request for flight {} with seats {}",
                req.getFlightId(), req.getSeatNumbers());

        //  passenger ↔ seat count
        if (req.getPassengerCount() != req.getSeatNumbers().size()) {
            throw new IllegalArgumentException(
                    "Passenger count must match seat count"
            );
        }

        //  no duplicate seats
        if (new HashSet<>(req.getSeatNumbers()).size()
                != req.getSeatNumbers().size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat selection is not allowed"
            );
        }

        //one booking per user per flight
        bookingRepository
                .findByFlightIdAndEmail(
                        req.getFlightId(),
                        req.getEmail()
                )
                .ifPresent(b -> {
                    throw new IllegalStateException(
                            "Booking already exists for this user and flight"
                    );
                });

        // lock seats (authoritative)
        flightClient.lockSeats(
                req.getFlightId(),
                req.getSeatNumbers()
        );

        // Reduce available seat count
        boolean reduced = flightClient.reduceSeats(
                req.getFlightId(),
                req.getPassengerCount()
        );

        if (!reduced) {
            // rollback seat lock
            flightClient.releaseSeats(
                    req.getFlightId(),
                    req.getSeatNumbers()
            );
            throw new FlightServiceDownException(
                    "Flight Service DOWN"
            );
        }

        // Save booking
        Booking booking = new Booking();
        booking.setPnr(PnrGenerator.generate());
        booking.setFlightId(req.getFlightId());
        booking.setPassengerName(req.getPassengerName());
        booking.setEmail(req.getEmail());
        booking.setPassengerCount(req.getPassengerCount());
        booking.setSeatNumbers(req.getSeatNumbers());
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(new Date());

        bookingRepository.save(booking);

        emailProducer.sendEmail(
                new EmailNotification(
                        booking.getEmail(),
                        "Booking Confirmed",
                        "PNR: " + booking.getPnr()
                                + "\nSeats: " + booking.getSeatNumbers()
                )
        );

        return booking;
    }
   

    public Booking bookFallback(
            BookingRequest req,
            Throwable ex
    ) {

        log.warn("Booking fallback triggered: {}", ex.getMessage());

        Booking fallback = new Booking();
        fallback.setPnr(PnrGenerator.generate());
        fallback.setFlightId(req.getFlightId());
        fallback.setPassengerName(req.getPassengerName());
        fallback.setEmail(req.getEmail());
        fallback.setPassengerCount(req.getPassengerCount());
        fallback.setSeatNumbers(req.getSeatNumbers());
        fallback.setStatus("FAILED");
        fallback.setBookingDate(new Date());

        return fallback;
    }



    @Transactional
    public Booking cancelBooking(String id) {

        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id)
                );

        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        //  Release seats in FlightService
        flightClient.releaseSeats(
                booking.getFlightId(),
                booking.getSeatNumbers()
        );

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        flightClient.increaseSeats(
                booking.getFlightId(),
                booking.getPassengerCount()
        );

        emailProducer.sendEmail(
                new EmailNotification(
                        booking.getEmail(),
                        "Booking Cancelled",
                        "PNR: " + booking.getPnr()
                )
        );

        return booking;
    }

    

    @Transactional
    public Booking cancelBookingByPnr(String pnr) {

        Booking booking = bookingRepository
                .findByPnr(pnr)
                .orElseThrow(() ->
                        new BookingNotFoundException("PNR: " + pnr)
                );

        if ("CANCELLED".equals(booking.getStatus())) {
            return booking;
        }

        FlightInternalDTO flight =
                flightClient.getFlightById(
                        booking.getFlightId()
                );

        if (flight == null || flight.getDate() == null) {
            throw new FlightServiceDownException(
                    "Unable to verify flight schedule"
            );
        }

        LocalDate flightDate = LocalDate.parse(
                flight.getDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        long hoursLeft = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                flightDate.atStartOfDay()
        );

        if (hoursLeft < 24) {
            throw new CancellationNotAllowedException(
                    "Ticket cannot be cancelled within 24 hours of departure"
            );
        }

        //  Release seats
        flightClient.releaseSeats(
                booking.getFlightId(),
                booking.getSeatNumbers()
        );

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        flightClient.increaseSeats(
                booking.getFlightId(),
                booking.getPassengerCount()
        );

        emailProducer.sendEmail(
                new EmailNotification(
                        booking.getEmail(),
                        "Booking Cancelled",
                        "PNR: " + booking.getPnr()
                )
        );

        return booking;
    }

    

    public Booking getBooking(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new BookingNotFoundException(id)
                );
    }

    public Booking getBookingByPnr(String pnr) {
        return bookingRepository.findByPnr(pnr)
                .orElseThrow(() ->
                        new BookingNotFoundException("PNR: " + pnr)
                );
    }

    public List<Booking> getBookingsByEmail(String email) {
        return bookingRepository.findByEmail(email);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}
