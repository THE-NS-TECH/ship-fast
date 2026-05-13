package com.shipfast.service;

import com.shipfast.domain.DeliveryTrip;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.repository.DeliveryTripRepository;
import com.shipfast.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeliveryTripService {

    private final DeliveryTripRepository tripRepository;
    private final VehicleRepository vehicleRepository;

    public DeliveryTripService(DeliveryTripRepository tripRepository, VehicleRepository vehicleRepository) {
        this.tripRepository = tripRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public DeliveryTrip createTrip(CreateTripRequest request) {
        // TODO: Implement trip creation validation and persistence
        // 1. Destination cannot be null or empty
        // 2. Distance must be greater than 0
        // 3. Cargo weight must be greater than 0
        // 4. Cargo weight cannot exceed vehicle's max capacity
        // Throw InsufficientCapacityException if weight limit exceeded
        return null;
    }

    @Transactional
    public DeliveryTrip completeTrip(Long tripId) {
        // TODO: Implement trip completion logic
        // 1. Calculate fuel consumption: DistanceKm / VehicleFuelEfficiency
        // 2. Update fuelConsumptionLiters and status to COMPLETED
        return null; 
    }

    public List<DeliveryTrip> getAllTrips() {
        return tripRepository.findAll();
    }
}
