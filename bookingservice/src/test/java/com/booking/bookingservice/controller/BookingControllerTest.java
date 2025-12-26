package com.booking.bookingservice.controller;

import com.booking.bookingservice.dto.BookingRequest;
import com.booking.bookingservice.dto.BookingResponse;
import com.booking.bookingservice.exception.GlobalExceptionHandler;
import com.booking.bookingservice.model.Booking;
import com.booking.bookingservice.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "eureka.client.enabled=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void testCreateBooking_Success() throws Exception {

        BookingRequest request = new BookingRequest(
                "FL123",
                "John Doe",
                "john@gmail.com",
                2,
                List.of("A1", "A2")
        );

        Booking booking = new Booking();
        booking.setPnr("PNR123");
        booking.setFlightId("FL123");
        booking.setPassengerName("John Doe");
        booking.setEmail("john@gmail.com");
        booking.setPassengerCount(2);
        booking.setSeatNumbers(List.of("A1", "A2"));
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(new Date());

        when(bookingService.bookTicket(any())).thenReturn(booking);

        mockMvc.perform(post("/booking/create")
                        .header("X-User-Email", "john@gmail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pnr").value("PNR123"))
                .andExpect(jsonPath("$.flightId").value("FL123"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.seatNumbers.length()").value(2));
    }


    @Test
    void testGetBookingById() throws Exception {

        Booking booking = new Booking();
        booking.setId("B123");
        booking.setPassengerName("Alice");

        when(bookingService.getBooking("B123")).thenReturn(booking);

        mockMvc.perform(get("/booking/B123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("B123"))
                .andExpect(jsonPath("$.passengerName").value("Alice"));
    }


    @Test
    void testCancelBooking() throws Exception {

        Booking cancelled = new Booking();
        cancelled.setStatus("CANCELLED");

        when(bookingService.cancelBooking("B500")).thenReturn(cancelled);

        mockMvc.perform(put("/booking/cancel/B500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }


    @Test
    void testCreateBooking_FlightServiceDown() throws Exception {

        BookingRequest request = new BookingRequest(
                "FL555",
                "Ravi",
                "ravi@mail.com",
                1,
                List.of("B1")
        );

        when(bookingService.bookTicket(any()))
                .thenThrow(new RuntimeException("Flight Service is DOWN"));

        mockMvc.perform(post("/booking/create")
                        .header("X-User-Email", "ravi@mail.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Flight Service is DOWN"));
    }
}
