package com.booking.bookingservice.service;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.EmailNotification;
import com.booking.bookingservice.exception.BookingNotFoundException;
import com.booking.bookingservice.exception.FlightServiceDownException;
import com.booking.bookingservice.feign.FlightClient;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.producer.EmailProducer;
import com.booking.bookingservice.repo.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "eureka.client.enabled=false"
})
class BookingServiceTest {

    private BookingRepository bookingRepository;
    private FlightClient flightClient;
    private EmailProducer emailProducer;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        flightClient = mock(FlightClient.class);
        emailProducer = mock(EmailProducer.class);

        bookingService =
                new BookingService(bookingRepository, flightClient, emailProducer);
    }


    @Test
    void testBookTicket_Success() {

        BookingRequest request = new BookingRequest(
                "FL123",
                "John",
                "john@gmail.com",
                2,
                List.of("A1", "A2")
        );

        when(flightClient.reduceSeats("FL123", 2)).thenReturn(true);

        Booking result = bookingService.bookTicket(request);

        assertEquals("CONFIRMED", result.getStatus());
        assertEquals(2, result.getSeatNumbers().size());

        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(emailProducer, times(1))
                .sendEmail(any(EmailNotification.class));
    }

    @Test
    void testBookTicket_FlightServiceDown() {

        BookingRequest request = new BookingRequest(
                "FL123",
                "John",
                "john@gmail.com",
                2,
                List.of("A1", "A2")
        );

        when(flightClient.reduceSeats("FL123", 2)).thenReturn(false);

        FlightServiceDownException ex =
                assertThrows(FlightServiceDownException.class,
                        () -> bookingService.bookTicket(request));

        assertEquals("Flight Service DOWN", ex.getMessage());
    }


    @Test
    void testCancelBooking_Success() {

        Booking existing = new Booking();
        existing.setId("B1");
        existing.setFlightId("FL123");
        existing.setSeatNumbers(List.of("A1", "A2"));
        existing.setStatus("CONFIRMED");

        when(bookingRepository.findById("B1"))
                .thenReturn(Optional.of(existing));

        Booking result = bookingService.cancelBooking("B1");

        assertEquals("CANCELLED", result.getStatus());

        verify(flightClient, times(1))
                .increaseSeats("FL123", 2);
        verify(emailProducer, times(1))
                .sendEmail(any(EmailNotification.class));
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {

        Booking existing = new Booking();
        existing.setId("B1");
        existing.setStatus("CANCELLED");

        when(bookingRepository.findById("B1"))
                .thenReturn(Optional.of(existing));

        Booking result = bookingService.cancelBooking("B1");

        assertEquals("CANCELLED", result.getStatus());

        verify(flightClient, never())
                .increaseSeats(anyString(), anyInt());
    }

    @Test
    void testCancelBooking_NotFound() {

        when(bookingRepository.findById("B404"))
                .thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class,
                () -> bookingService.cancelBooking("B404"));
    }
}
