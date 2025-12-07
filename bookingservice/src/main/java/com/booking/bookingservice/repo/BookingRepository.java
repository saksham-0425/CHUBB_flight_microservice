package com.booking.bookingservice.repo;

import com.booking.bookingservice.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByEmail(String email);
    Optional<Booking> findByFlightIdAndEmail(String flightId, String email);
}
