package com.shipfast.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipfast.domain.DeliveryTrip;
import com.shipfast.domain.OutboxEvent;
import com.shipfast.domain.TripStatus;
import com.shipfast.domain.Vehicle;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.exception.InsufficientCapacityException;
import com.shipfast.exception.TripAlreadyCompletedException;
import com.shipfast.repository.DeliveryTripRepository;
import com.shipfast.repository.OutboxEventRepository;
import com.shipfast.repository.VehicleRepository;
import io.micrometer.observation.annotation.Observed;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Observed(name = "delivery_trip_service")
public class DeliveryTripService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryTripService.class);

    private final DeliveryTripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public DeliveryTripService(DeliveryTripRepository tripRepository, VehicleRepository vehicleRepository,
                               OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Observed(name = "create_trip", contextualName = "create-trip")
    public DeliveryTrip createTrip(CreateTripRequest request) {
        log.info("Creating trip for vehicleId: {} with destination: {}", request.vehicleId(), request.destination());
        
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + request.vehicleId()));

        if (request.cargoWeightKg() > vehicle.getMaxCapacityKg()) {
            log.warn("Trip creation failed. Cargo weight {}kg exceeds vehicle capacity {}kg for vehicleId: {}", 
                     request.cargoWeightKg(), vehicle.getMaxCapacityKg(), vehicle.getId());
            throw new InsufficientCapacityException(
                    "Cargo weight " + request.cargoWeightKg() + "kg exceeds vehicle max capacity of " + vehicle.getMaxCapacityKg() + "kg");
        }

        DeliveryTrip trip = new DeliveryTrip();
        trip.setVehicle(vehicle);
        trip.setDestination(request.destination());
        trip.setCargoWeightKg(request.cargoWeightKg());
        trip.setDistanceKm(request.distanceKm());
        trip.setStatus(TripStatus.PENDING);

        DeliveryTrip savedTrip = tripRepository.save(trip);
        
        saveOutboxEvent(savedTrip, "TRIP_CREATED");
        
        log.info("Trip created successfully with id: {}", savedTrip.getId());
        return savedTrip;
    }

    @Transactional
    @Observed(name = "complete_trip", contextualName = "complete-trip")
    public DeliveryTrip completeTrip(Long tripId) {
        log.info("Completing trip with id: {}", tripId);
        
        DeliveryTrip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new EntityNotFoundException("Trip not found with id: " + tripId));

        if (trip.getStatus() == TripStatus.COMPLETED) {
            log.warn("Attempted to complete an already completed trip: {}", tripId);
            throw new TripAlreadyCompletedException("Trip is already completed with id: " + tripId);
        }

        double fuelConsumption = trip.getDistanceKm() / trip.getVehicle().getFuelEfficiency();
        trip.setFuelConsumptionLiters(fuelConsumption);
        trip.setStatus(TripStatus.COMPLETED);

        DeliveryTrip savedTrip = tripRepository.save(trip);
        
        saveOutboxEvent(savedTrip, "TRIP_COMPLETED");
        
        log.info("Trip {} completed. Fuel consumption: {}L", savedTrip.getId(), fuelConsumption);
        return savedTrip;
    }

    private void saveOutboxEvent(DeliveryTrip trip, String eventType) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", trip.getId());
            payload.put("vehicleId", trip.getVehicle().getId());
            payload.put("destination", trip.getDestination());
            payload.put("status", trip.getStatus());
            
            OutboxEvent event = new OutboxEvent(
                    "DeliveryTrip", 
                    trip.getId().toString(), 
                    eventType, 
                    objectMapper.writeValueAsString(payload)
            );
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload for trip: {}", trip.getId(), e);
            throw new RuntimeException("Failed to serialize outbox event", e);
        }
    }

    @Transactional(readOnly = true)
    public List<DeliveryTrip> getAllTrips() {
        log.debug("Fetching all trips");
        return tripRepository.findAll();
    }
}
