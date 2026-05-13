package com.shipfast.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "delivery_trips")
public class DeliveryTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotBlank
    @Column(nullable = false)
    private String destination;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double cargoWeightKg;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double distanceKm;

    @Column
    private Double fuelConsumptionLiters;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.PENDING;

    public DeliveryTrip() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Double getCargoWeightKg() { return cargoWeightKg; }
    public void setCargoWeightKg(Double cargoWeightKg) { this.cargoWeightKg = cargoWeightKg; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getFuelConsumptionLiters() { return fuelConsumptionLiters; }
    public void setFuelConsumptionLiters(Double fuelConsumptionLiters) { this.fuelConsumptionLiters = fuelConsumptionLiters; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
}
