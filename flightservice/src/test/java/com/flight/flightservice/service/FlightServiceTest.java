package com.flight.flightservice.service;

import com.flight.flightservice.exception.FlightNotFoundException;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.repo.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightServiceTest {

    private FlightRepository repository;
    private FlightService service;

    @BeforeEach
    void setup() {
        repository = mock(FlightRepository.class);
        service = new FlightService(repository);
    }

    @Test
    void testAddFlight_Success() {
        Flight flight = new Flight();
        flight.setSource("Delhi");
        flight.setDestination("Mumbai");

        when(repository.save(any())).thenReturn(flight);

        Flight result = service.addFlight(flight);

        assertEquals("Mumbai", result.getDestination());
        verify(repository, times(1)).save(flight);
    }

    @Test
    void testAddFlight_SourceDestinationSame_ShouldThrowException() {
        Flight flight = new Flight();
        flight.setSource("Delhi");
        flight.setDestination("Delhi");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addFlight(flight)
        );

        assertEquals("Source and destination cannot be same", ex.getMessage());
    }

    @Test
    void testGetFlightSuccess() {
        Flight flight = new Flight();
        flight.setId("123");

        when(repository.findById("123")).thenReturn(Optional.of(flight));

        Flight result = service.getFlight("123");

        assertEquals("123", result.getId());
    }

    @Test
    void testGetFlightNotFound() {
        when(repository.findById("999")).thenReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> service.getFlight("999"));
    }

    @Test
    void testReduceSeatsSuccess() {
        Flight flight = new Flight();
        flight.setAvailableSeats(50);

        when(repository.findById("1")).thenReturn(Optional.of(flight));

        boolean result = service.reduceSeats("1", 10);

        assertTrue(result);
        assertEquals(40, flight.getAvailableSeats());
        verify(repository).save(flight);
    }

    @Test
    void testReduceSeatsFailure() {
        Flight flight = new Flight();
        flight.setAvailableSeats(5);

        when(repository.findById("1")).thenReturn(Optional.of(flight));

        boolean result = service.reduceSeats("1", 10);

        assertFalse(result);
        verify(repository, never()).save(flight);
    }

    @Test
    void testIncreaseSeats() {
        Flight flight = new Flight();
        flight.setAvailableSeats(20);

        when(repository.findById("55")).thenReturn(Optional.of(flight));

        service.increaseSeats("55", 5);

        assertEquals(25, flight.getAvailableSeats());
        verify(repository).save(flight);
    }
}
