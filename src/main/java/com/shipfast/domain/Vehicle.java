package com.shipfast.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String licensePlate;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double maxCapacityKg;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Double fuelEfficiency; // km/L

    public Vehicle() {}

    public Vehicle(String licensePlate, Double maxCapacityKg, Double fuelEfficiency) {
        this.licensePlate = licensePlate;
        this.maxCapacityKg = maxCapacityKg;
        this.fuelEfficiency = fuelEfficiency;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public Double getMaxCapacityKg() { return maxCapacityKg; }
    public void setMaxCapacityKg(Double maxCapacityKg) { this.maxCapacityKg = maxCapacityKg; }

    public Double getFuelEfficiency() { return fuelEfficiency; }
    public void setFuelEfficiency(Double fuelEfficiency) { this.fuelEfficiency = fuelEfficiency; }
}
