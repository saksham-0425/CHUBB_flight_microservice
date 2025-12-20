package com.flight.flightservice.controller;

import com.flight.flightservice.dto.FlightResponseDTO;
import com.flight.flightservice.model.Flight;
import com.flight.flightservice.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    private final FlightService service;

    public FlightController(FlightService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a new flight", description = "Creates a new flight record")
    @ApiResponse(responseCode = "201", description = "Flight created successfully")
    @PostMapping("/add")
    public ResponseEntity<String> addFlight(@Valid @RequestBody Flight flight) {
    	log.info("Request received to add flight");
    	
    	if (flight.getSource().equalsIgnoreCase(flight.getDestination())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Source and destination cannot be same");
        }

        service.addFlight(flight);

        return new ResponseEntity<>("success", HttpStatus.CREATED);
    }


    @GetMapping("/search")
    @Operation(summary = "Search flights", description = "Searches flights by source, destination and date")
    public List<Flight> searchFlights(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String date) {
        log.info("Search request received for flights {} -> {} on {}", source, destination, date);
        return service.searchFlights(source, destination, date);
    }

    @GetMapping("/internal/{id}/check")
    @Operation(summary = "Check flight availability (internal API)")
    public boolean checkAvailability(@PathVariable String id) {
        log.info("Checking availability for flight {}", id);
        Flight flight = service.getFlight(id);
        return flight.getAvailableSeats() > 0;
    }

    @PutMapping("/internal/{id}/reduce")
    @Operation(summary = "Reduce seats (internal API)")
    public boolean reduceSeats(@PathVariable String id, @RequestParam int count) {
        log.info("Request received to reduce seats for flight {}", id);
        return service.reduceSeats(id, count);
    }

    @PutMapping("/internal/{id}/increase")
    @Operation(summary = "Increase seats (internal API)")
    public void increaseSeats(@PathVariable String id, @RequestParam int count) {
        log.info("Request received to increase seats for flight {}", id);
        service.increaseSeats(id, count);
    }
    
    @GetMapping("/internal/{id}")
    public Flight getFlightInternal(@PathVariable String id) {
        return service.getFlight(id);
    }
    
    @PostMapping(value = "/upload-json", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadJson(@RequestParam("file") MultipartFile file) {
        try {
            int count = service.uploadFlightsJson(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(count + " flights uploaded successfully");
        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Failed to process JSON file");
        }
    }



}
