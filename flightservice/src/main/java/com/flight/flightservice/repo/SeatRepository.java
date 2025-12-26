package com.flight.flightservice.repo;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.flight.flightservice.model.*;
import java.util.*;

public interface SeatRepository
extends MongoRepository<Seat, String> {
	
	 List<Seat> findByFlightId(String flightId);

	 List<Seat> findByFlightIdAndSeatNumberIn(
		        String flightId,
		        List<String> seatNumbers
		);

@Query(value = "{ 'flightId': ?0, 'seatNumber': { $in: ?1 }, 'booked': false }", count = true)
long countAvailableSeats(String flightId, List<String> seatNumbers);

default boolean areSeatsAvailable(String flightId, List<String> seatNumbers) {
    return countAvailableSeats(flightId, seatNumbers) == seatNumbers.size();
}


@Query("{ 'flightId': ?0, 'seatNumber': { $in: ?1 } }")
@Update("{ '$set': { 'booked': true } }")
void lockSeats(String flightId, List<String> seatNumbers);


@Query("{ 'flightId': ?0, 'seatNumber': { $in: ?1 } }")
@Update("{ '$set': { 'booked': false } }")
void releaseSeats(String flightId, List<String> seatNumbers);
}
