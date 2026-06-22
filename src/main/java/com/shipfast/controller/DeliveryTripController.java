package com.shipfast.controller;

import com.shipfast.domain.DeliveryTrip;
import com.shipfast.dto.CreateTripRequest;
import com.shipfast.service.DeliveryTripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class DeliveryTripController {

    private final DeliveryTripService tripService;

    public DeliveryTripController(DeliveryTripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryTrip createTrip(@RequestBody @Valid CreateTripRequest request) {
        return tripService.createTrip(request);
    }

    @PostMapping("/{id}/complete")
    public DeliveryTrip completeTrip(@PathVariable Long id) {
        return tripService.completeTrip(id);
    }

    @GetMapping
    public List<DeliveryTrip> listTrips() {
        return tripService.getAllTrips();
    }
}
